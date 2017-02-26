/** 
 * @file timer.h
 *
 * @copydoc timer.c
 */
#ifndef _TIMER_H_
#define _TIMER_H_

void timer_8bit_start(uint16_t prescaler, uint8_t ovf_interrupts);
void timer_8bit_stop(void);
uint16_t timer_get_8bit_ovf(void);
void timer_16bit_start(uint16_t prescaler);
void timer_16bit_stop(void);
void timer_16bit_reset(void);
uint16_t timer_16bit_value(void);

#endif
