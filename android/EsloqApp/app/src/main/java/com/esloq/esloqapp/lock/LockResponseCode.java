package com.esloq.esloqapp.lock;

/**
 * Enum that specifies the opcodes used for the responses received froman esloq. It allows the
 * lock and the phone to interpret the rest of the message correctly.
 */
enum LockResponseCode {

    UNLOCKED(0), LOCKED(1), TICKET_RCV_SUCCESS(4), TICKET_RCV_FAILURE(5),
    TICKET_RCV_SUCCESS_LOW_BAT(6), INVALID_REQUEST(255);

    /**
     * Integer value of the opcode.
     */
    private final int intValue;

    /**
     * Instantiates an opcode from an integer.
     *
     * @param number    integer from which the opcode is built.
     */
    LockResponseCode(int number){
        this.intValue = number;
    }

    /**
     * Returns the opcode object corresponding with the given integer.
     *
     * @param integer   integer that corresponds with the opcode to be returned
     * @return  Opcode object that corresponds with the given integer
     */
    public static LockResponseCode fromInteger(int integer) {

        for (LockResponseCode lockResponseCode : LockResponseCode.values()) {
            if(lockResponseCode.getIntValue() == integer) {
                return lockResponseCode;
            }
        }
        throw new IllegalArgumentException("No LockResponseCode exists for integer : " + integer);
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
