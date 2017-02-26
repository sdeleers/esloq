
/** 
 * @file motor_control.c
 *
 * Control the motor.
 *
 * This module allows for abstraction of dc_motor.c and encoder.c to provide 
 * an easy way to control the motor.
 *
 * Usage:
 *  1. initialize the motor controller with motor_ctrl_init()
 *  2. lock or unlock the door with motor_ctrl_lock() and motor_ctrl_unlock()
 *      respectively
 */

#include <avr/interrupt.h>
#include <util/delay.h>
#include "dc_motor.h"
#include "motor_control.h"
#include "timer.h"

#define MOTOR_TIMEOUT 10000
#define MOTOR_TIMER_THRESHOLD (F_CPU/256/256*MOTOR_TIMEOUT/1000-1)

void enable_current_sense() {
    PORTB |= (1 << PINB1); /* Enable current sense */
}

void disable_current_sense() {
    PORTB &= ~(1 << PINB1); /* Disable current sense */
}

/**
 * Rotate the motor clockwise until it gets jammed or timeout is reached.
 */
void motor_ctrl_rotate_cw()
{
    dc_motor_turn_cw();
    timer_8bit_start(256, 1); /* Prescaler 256, enable overflow interrupts */
    _delay_ms(400); /* Startup current */
    enable_current_sense();
    while(bit_is_set(PINB,0) && (timer_get_8bit_ovf() < MOTOR_TIMER_THRESHOLD));
    timer_8bit_stop();
    disable_current_sense();
    dc_motor_halt();    
} 

/**
 * Rotate the motor counter-clockwise until it gets jammed or timeout is reached.
 */
void motor_ctrl_rotate_ccw()
{
    dc_motor_turn_ccw();
    timer_8bit_start(256, 1); /* Prescaler 256, enable overflow interrupts */
    _delay_ms(400); /* Startup current */
    enable_current_sense();
    while(bit_is_set(PINB,0) && (timer_get_8bit_ovf() < MOTOR_TIMER_THRESHOLD));
    timer_8bit_stop();
    disable_current_sense();
    dc_motor_halt();    
}

/**
 * Initialize the motor controller.
 *
 * It consists of initializing the encoder, DC motor and ADC.
 */
void motor_ctrl_init()
{
    dc_motor_init();
    /* Set current sense enable as output */
    DDRB |= (1 << PINB1);
    /* Set current sense alert as input  */
    DDRB &= ~(1 << PINB0);
    /* Pull alert high */
    PORTB |= (1 << PINB0); 
    disable_current_sense();
}

