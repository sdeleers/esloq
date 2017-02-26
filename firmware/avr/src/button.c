/** 
 * @file button.c
 *
 */

#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include "button.h"
#include "requests.h"

volatile int8_t request = -1;

void button_init() {
    /* Set buttons as inputs and pulled high. */
    DDRC &= ~(1 << PINC1 | 1 << PINC2);
    PORTC |= (1 << PINC1 | 1 << PINC2);

    /* Enable external interrupts */
    PCICR |= (1 << PCIE1);
    PCMSK1 |= (1 << PCINT9 | 1 << PCINT10);
}

ISR(PCINT1_vect) 
{
    if(bit_is_clear(PINC, 1)) {
        request = ROTATE_CW;
    }
    else if(bit_is_clear(PINC, 2)) {
        request = ROTATE_CCW;
    }
}
