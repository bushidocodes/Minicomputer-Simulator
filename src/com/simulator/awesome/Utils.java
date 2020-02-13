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

}
