/** 
 * @file spi.h
 *
 * @copydoc spi.c
 */

#ifndef _SPI_H_
#define _SPI_H_

#include <avr/io.h>

void spi_master_init();
void spi_print_string(const char *s);
void spi_print_int(const int i);
void spi_print_flash_string(const __flash char *s);

#endif
