/** 
 * @file spi.c
 *
 */
#include <stdlib.h>
#include "spi.h"

#define SCK PINB5
#define MOSI PINB3
#define SS PINB2

#define MAX_INT_DIGITS 10 /**< Maximum number of digits of printed integer. */

void spi_master_init() {
    DDRB |= (1 << SS) | (1 << MOSI) | (1 << SCK);
    SPCR = (1 << SPE) | (1 << MSTR) | (1 << SPR1) | (1 << SPR0);
}

uint8_t spi_print_char(const char data) {
    /*transmit the byte to be sent */
    SPDR = data;
    /* wait for the transfer to complete */
    while (!(SPSR & (1<<SPIF)));
    /* return byte read from buffer */
    return SPDR;
}

void spi_print_string(const char *s) {
    while(*s) spi_print_char(*s++);
    spi_print_char('\r');
    spi_print_char('\n');
}

void spi_print_int(int i) {
    char s[MAX_INT_DIGITS];
    itoa(i, s, 10);
    spi_print_string(s);
}

void spi_print_flash_string(const __flash char *s) {
    while(*s) spi_print_char(*s++);
    spi_print_char('\r');
    spi_print_char('\n');
}
