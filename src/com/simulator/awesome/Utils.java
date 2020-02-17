package com.simulator.awesome;

public class Utils {
    /**
     * Unsigned shifts don't work for Integrals other than Integers.
     * This function gets around it by left-packing the short as an Int before shifting
     * @param source - The original unshifted short
     * @param shift - The number of positions we want to unsigned right shift
     * @return the shifted short
     */
    public static short short_unsigned_right_shift(short source, int shift) {
        int temp = source;
        temp <<= 16; // Shift to the upper half of the int temporarily
        temp >>>= (shift % 16);
        temp >>= 16;
        return (short)temp;
    }

    /**
     * A Helper getter for bit arrays
     * @param bitArray - The integral value that we are treating as an array of bits
     * @param offset - The offset from the least significant bit. 0 is b0001
     * @return - True if bit is set
     */
    public static boolean getNthLeastSignificantBit(int bitArray, int offset) {
        int getterMask = (0b00000001<<offset);
        return (bitArray & getterMask) == getterMask;
    }

    /**
     * A Helper setting for bit arrays
     * @param bitArray - The integral that we are treating as an array of bits
     * @param offset - The offset from the least significant bit. 0 is b0001
     * @param isSet - The resulting bit array after manipulating the bit
     */
    public  static int setNthLeastSignificantBit(int bitArray, int offset, boolean isSet) {
        int setterMask = (0b00000001<<offset);
        return isSet ? (bitArray | setterMask) : (bitArray & ~setterMask);
    }

}
