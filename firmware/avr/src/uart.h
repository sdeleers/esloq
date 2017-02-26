/**
 * @file uart.h
 *
 * @copydoc uart.c
 */

#ifndef _UART_H_
#define _UART_H_

#include <stdint.h>

#define RX_MAX_BUFFER_SIZE 256

int8_t uart_tx(uint8_t len1, uint8_t* data1, uint8_t len2, uint8_t* data2, uint16_t timeout_ms);
int8_t uart_rx(uint8_t len, uint8_t* data, uint16_t timeout_ms);
uint8_t uart_clear_to_send(void);
void uart_ready_to_receive(void);
void uart_not_ready_to_receive(void);
void uart_enable(void);
void uart_disable(void);
void uart_init(uint16_t baud);

#endif

