package com.esloq.esloqapp.util;

import android.os.Build;

/**
 * Class containing various helper methods.
 */
public class Tools {
    /**
     * Concatenates variable number of byte arrays.
     *
     * @param byteArrays    Byte arrays to concatenate.
     * @return Concatenated byte array.
     */
    public static byte[] concatenateBytes(byte[]... byteArrays) {
        int totalLength = 0;
        for (byte[] byteArray: byteArrays) {
            totalLength += byteArray.length;
        }

        byte[] concatenatedBytes = new byte[totalLength];
        int offset = 0;
        for (byte[] byteArray: byteArrays) {
            System.arraycopy(byteArray, 0, concatenatedBytes, offset, byteArray.length);
            offset += byteArray.length;
        }
        return concatenatedBytes;
    }

    /**
     * Increment little endian byte array.
     *
     * @param array The byte array to increment.
     */
    public static void incrementByteArray(byte[] array) {
        incrementAtIndex(array, array.length-1);
    }

    /**
     * Increment little endian byte array at a given index
     *
     * @param array The byte array to increment.
     * @param index The index at which to increment the byte array.
     */
    private static void incrementAtIndex(byte[] array, int index) {
        if (array[index] == Byte.MAX_VALUE) {
            array[index] = 0;
            if(index > 0) {
                incrementAtIndex(array, index - 1);
            }
        }
        else {
            array[index]++;
        }
    }

    /**
     * Get the name of the device.
     *
     * @return name of the device.
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
