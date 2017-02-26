/** 
 * @file dc_motor.c
 *
 * Control the DC motor.
 *
 * This module provides a layer of abstraction for controlling a DC motor.
 *
 * Usage:
 *  1. initialize the control of the DC motor with dc_motor_init()
 *  2. rotate the DC motor with dc_motor_turn_cw() or dc_motor_turn_ccw()
 *  3. stop the rotation of the DC motor with dc_motor_halt()
 */

#include <avr/io.h>
#include "dc_motor.h"

#define M_PORT PORTD /**< Motor pins port. */
#define M_DDR DDRD /**< Motor pins data direction register. */
#define M1 PIND5 /**< Motor pin 1. */
#define M2 PIND6 /** Motor pin 2. */

/**
 * Rotate the DC motor clockwise.
 */
void dc_motor_turn_cw()
{
    /* Set motor control 2 to zero to stop motor if turning */
    M_PORT &= ~(1<<M2);
    /* Start rotating */
    M_PORT |= 1<<M1;
}

/**
 * Rotate the DC motor counter clockwise.
 */
void dc_motor_turn_ccw()
{
    /* Set motor control 1 to zero to stop motor if turning */
    M_PORT &= ~(1<<M1);
    /* Start rotating */
    M_PORT |= 1<<M2;
}

/**
 * Halt the rotation of the DC motor.
 */
void dc_motor_halt()
{
    /* Make both motor control pins low */
    M_PORT &= ~(1<<M1 | 1<<M2);
}

/**
 * Initialize the DC motor control.
 *
 * Initialize a DC motor connected to PC4 and PC5.
 */
void dc_motor_init()
{
    /* Motor control pins as outputs */
    M_DDR |= 1<<M1 | 1<<M2;
    /* Set pins low */
    M_PORT &= ~(1<<M1 | 1<<M2);
}

