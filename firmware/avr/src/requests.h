#ifndef _REQUESTS_H_
#define _REQUESTS_H_

#define ROTATE_CCW 0
#define ROTATE_CW  1

#include <stdint.h>

typedef uint8_t RSP_CODE;

RSP_CODE rotate_motor_ccw();
RSP_CODE rotate_motor_cw();

#endif

