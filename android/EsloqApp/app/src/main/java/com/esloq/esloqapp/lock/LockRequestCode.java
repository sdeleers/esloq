package com.esloq.esloqapp.lock;

/**
 * Enum that specifies the opcodes used for the requests sent to an esloq. It allows the lock and
 * the phone to interpret the rest of the message correctly.
 */
enum LockRequestCode {

    ROTATE_COUNTER_CLOCKWISE(0), ROTATE_CLOCKWISE(1);

    /**
     * Integer value of the opcode.
     */
    private final int intValue;

    /**
     * Instantiates an opcode from an integer.
     *
     * @param number    integer from which the opcode is built.
     */
    LockRequestCode(int number){
        this.intValue = number;
    }

    /**
     * Returns the Opcode object corresponding with the given integer.
     *
     * @param integer   integer that corresponds with the opcode to be returned
     * @return  Opcode object that corresponds with the given integer
     */
    public static LockRequestCode fromInteger(int integer) {

        for (LockRequestCode lockRequestCode : LockRequestCode.values()) {
            if(lockRequestCode.getIntValue() == integer) {
                return lockRequestCode;
            }
        }
        throw new IllegalArgumentException("No LockRequestCode exists for integer : " + integer);
    }

    /**
     * Gets the Integer value of the opcode.
     *
     * @return  integer value of the opcode
     */
    public int getIntValue(){
        return intValue;
    }
}
