/** 
 * @file motor_control.h
 *
 * @copydoc motor_control.c
 */

#ifndef _MOTOR_CONTROL_H_
#define _MOTOR_CONTROL_H_

#include <stdint.h>

void motor_ctrl_init(void);
void motor_ctrl_rotate_cw(void);
void motor_ctrl_rotate_ccw(void);

#endif

