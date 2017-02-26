/** 
 * @file timer.c
 *
 */
#include <avr/interrupt.h>

/* Number of timers 8bit timer has overflown */
volatile uint16_t timer_8bit_ovf = 0;

void timer_8bit_start(uint16_t prescaler, uint8_t ovf_interrupts) {
    switch(prescaler) {
        case 256:
            TCCR0B |= 1<<CS02;
            break;
        default:
            break;
    }
    TCNT0 = 0;
    if (ovf_interrupts) {
        timer_8bit_ovf = 0;
        TIMSK0 |= (1 << TOIE0);
    }
}

void timer_8bit_stop() {
    TCCR0B &= ~(1<<CS02 | 1<<CS01 | 1<<CS00);
}

uint16_t timer_get_8bit_ovf() {
    uint16_t timer_8bit_ovf_copy;
    cli();
    timer_8bit_ovf_copy = timer_8bit_ovf;
    sei();
    return timer_8bit_ovf_copy;
}

void timer_16bit_start(uint16_t prescaler) {
    switch(prescaler) {
        case 256:
            TCCR1B |= 1<<CS12;
            break;
        default:
            break;
    }
    TCNT1 = 0;
}

void timer_16bit_stop() {
    TCCR1B &= ~(1<<CS12 | 1<<CS11 | 1<<CS10);
}

void timer_16bit_reset() {
    cli();
    TCNT1 = 0;
    sei();
}

uint16_t timer_16bit_value() {
    uint16_t value;
    cli();
    value = TCNT1;
    sei();
    return value;
}

ISR(TIMER0_OVF_vect) {
    timer_8bit_ovf++;
}
