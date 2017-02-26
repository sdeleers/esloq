#include "motor_control.h"
#include "requests.h"
#include "responses.h"

RSP_CODE rotate_motor_ccw()
{
    motor_ctrl_rotate_ccw();
    return UNLOCKED;
}

RSP_CODE rotate_motor_cw()
{
    motor_ctrl_rotate_cw();
    return LOCKED;
}

