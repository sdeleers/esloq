/** 
 * @file uart.c
 *
 * Control UART communication.
 *
 * This module provides a layer of abstraction over the UART peripheral.
 *
 * Usage:
 *  1. initialize the UART with uart_init()
 *  2. set ready to receive before transmitting with uart_ready_to_receive()
 *  3. transmit or receive data with uart_tx() and uart_rx() respectively
 */

#include <avr/interrupt.h>
#include <avr/io.h>
#include "uart.h"
#include "timer.h"

/** UART RX data buffer. */
volatile uint8_t rxbuffer[RX_MAX_BUFFER_SIZE] = {0};

/** Start index of data in rxbuffer.  */
volatile uint8_t rxbuffer_start = 0; 

/** Next index to store data in rxbuffer. */
volatile uint8_t rxbuffer_next = 0; 

/** Size of the RX buffer. */
volatile uint8_t rxbuffer_size = 0; 

/**
 * Send a byte over UART.
 *
 * @param[in]   byte        The byte to be sent over UART.
 * @param[in]   timeout_ms  Time in ms to wait for clear to send.
 * @return 0 if data is received correctly, -1 if a timeout occured.
 */
int8_t send_byte(uint8_t byte, uint16_t timeout_ms)
{
    uint16_t prescaler = 256;
    /* Count threshold above wich a timeout occurs. */
    uint16_t cnt_threshold = F_CPU/prescaler*timeout_ms/1000 - 1;

    timer_16bit_start(prescaler); /* Start 16bit counter with prescaler 256. */
    /* Wait until clear to send. */
    /* Report timeout if it takes longer than timeout_ms */
    while (!uart_clear_to_send()) {
        if (timer_16bit_value() >= cnt_threshold) {
            timer_16bit_stop();
            return -1;
        }
    }
    timer_16bit_stop();

    /* Wait until clear to send. */
/*    while (!uart_clear_to_send());*/
    /* Wait until transmission buffer can be written. */
    while (!(UCSR0A & (1 << UDRE0))); 
    /* Write transmission buffer. */
    UDR0 = byte;
    return 0;
}

/**
 * Transmit data over UART.
 *
 * The data will be trasmitted as [len1 + len2 | data1 | data2]
 *
 * @param[in]   len1    Length of data1.
 * @param[in]   data1   First part of the data to be transmitted.
 * @param[in]   len2    Length of data2.
 * @param[in]   data2   Second part of the data to be transmitted.
 * @param[in]   timeout_ms  Time in ms to wait for clear to send.
 * @return 0 if data is received correctly, -1 if a timeout occured.
 */
int8_t uart_tx(uint8_t len1, uint8_t* data1, uint8_t len2, uint8_t* data2, uint16_t timeout_ms)
{
    uint8_t i;

    /* Send total data length. */
    send_byte(len1 + len2, timeout_ms);

    /* Send data. */
    for (i = 0; i < len1; i++) {
        if (send_byte(data1[i], timeout_ms)) {
            return -1;
        }
    }
    for (i = 0; i < len2; i++) {
        if (send_byte(data2[i], timeout_ms)) {
            return -1;
        }
    }
    return 0;
}

/**
 * Receive data from UART.
 *
 * Receives len bytes and writes them to data.
 *
 * @param[in]   len         The number of bytes to read.
 * @param[out]  data        The received data.
 * @param[in]   timeout_ms  Time in ms between two bytes before timing out.
 * @return 0 if data is received correctly, -1 if a timeout occured and 1
 *          if there is an error retreiving the data.
 */
int8_t uart_rx(uint8_t len, uint8_t* data, uint16_t timeout_ms)
{
    /* F_CPU in clock cycles / second
     * Prescaler in clock cycles / count
     * timeout_ms in second / 1000 
     * cnt_threshold in (cc/second) / (cc/cnt) * second = cnt
     */
    uint16_t prescaler = 256;
    uint16_t cnt_threshold = F_CPU/prescaler*timeout_ms/1000 - 1; /* Count threshold above wich a timeout occurs. */
    uint8_t i = 0;

    timer_16bit_start(prescaler); /* Start 16bit counter with prescaler 256. */

    while (i < len) {
        if (rxbuffer_size) {
            data[i] = rxbuffer[rxbuffer_start++];
            rxbuffer_start %= RX_MAX_BUFFER_SIZE;
            rxbuffer_size--;
            i++;
            timer_16bit_reset();
        }
        else if (timer_16bit_value() >= cnt_threshold) {
            timer_16bit_stop();
            if (i == 0) { /* No bytes received yet. */
                return -1; /* Timeout, no data received yet */
            }
            else { /* Only some bytes written to data. */
                return 1; /* Failed to receive all data. */
            }
        }
    } 
    timer_16bit_stop();
    return 0; /* All data received. */
}

uint8_t uart_clear_to_send()
{
    return !(PINC & (1<<PINC5));
}

void uart_ready_to_receive() 
{
    PORTC &= ~(1<<PINC4);
}

void uart_not_ready_to_receive() 
{
    PORTC |= 1<<PINC4;
}

void uart_enable()
{
    /* Enable TX and RX. */
    UCSR0B |= (1<<TXEN0) | (1<<RXEN0) | (1<<RXCIE0);

    /* Flow control pins */
    DDRC |= 1<<PINC4;       /* RTS, output */
    PORTC |= 1<<PINC4;      /* Set RTS high, not clear to reveive */
    DDRC &= ~(1<<PINC5);    /* CTS, input */ 
    PORTC |= 0<<PINC5;      /* pull up, defaults to not clear to send */

    /* Set buffer indices to first entry. */
    rxbuffer_start = 0;
    rxbuffer_next = 0;
    rxbuffer_size = 0;
}

void uart_disable()
{
    /* disable TX and RX. */
    UCSR0B = 0;
}

/**
 * Initialize UART commuication.
 *
 * Setup interrupt driven 8N1 UART where PD0 = RXD and PD1 = TXD. 
 *
 * @param[in]   baud    The baudrate for the UART communication.
 */
void uart_init(uint16_t baud)
{
    uint16_t UBRR = F_CPU/16/baud-1;

    UBRR0H = (uint8_t) (UBRR>>8);
    UBRR0L = (uint8_t) UBRR;
}

/**
 * Interrupt service routine for an interrupt on RXC.
 *
 * Gets triggered when there is a byte ready to be read in the receive buffer.
 * The byte gets added to the circular buffer rxbuffer.
 */
ISR(USART_RX_vect)
{
    rxbuffer[rxbuffer_next++] = UDR0;
    rxbuffer_next %= RX_MAX_BUFFER_SIZE;
    rxbuffer_size++;
}
