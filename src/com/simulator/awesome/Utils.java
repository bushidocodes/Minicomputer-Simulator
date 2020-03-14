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
        int getterMask = (0b0000000000000001<<offset);
        return (bitArray & getterMask) == getterMask;
    }

    /**
     * A Helper setting for bit arrays
     * @param bitArray - The integral that we are treating as an array of bits
     * @param offset - The offset from the least significant bit. 0 is b0001
     * @param isSet - The resulting bit array after manipulating the bit
     */
    public static short setNthLeastSignificantBit(short bitArray, int offset, boolean isSet) {
        short setterMask = (short)(0b0000000000000001<<offset);
        return isSet ? (short)(bitArray | setterMask) : (short)(bitArray & ~setterMask);
    }

    /**
     * A Helper getter for bit arrays
     * @param bitArray - The integral value that we are treating as an array of bits
     * @param offsetFromRightmostBit - The offset from the least significant bit. 0 is b0001
     * @param numberBits - The number of bits that encode this value
     * @return - value of bits
     */
    public static short getNthLeastSignificantBits(int bitArray, int offsetFromRightmostBit, int numberBits) {

        int singleBit = 0b0000000000000001;
        int getterMask = 0;
        for (int i = numberBits; i > 0; i--){
            getterMask |= (singleBit<<(i-1));
        }
        getterMask = (getterMask<<offsetFromRightmostBit);
        return short_unsigned_right_shift((short)(bitArray & getterMask), offsetFromRightmostBit);
    }

    /**
     * A Helper getter for bit arrays
     * @param bitArray - The integral value that we are treating as an array of bits
     * @param offsetFromRightmostBit - The offset from the least significant bit. 0 is b0001
     * @param numberBits - The number of bits that encode this value
     * @param value - the value that we want to set the bits to
     * @return - the updated bit array
     */
    public static short setNthLeastSignificantBits(short bitArray, int offsetFromRightmostBit, int numberBits, short value) {
        int singleBit = 0b0000000000000001;
        int setterMask = 0b0000000000000000;
        for (int i = numberBits; i > 0; i--){
            int positionedBit = (singleBit<<(i-1));
            setterMask |= positionedBit;
        }
        setterMask = (setterMask<<offsetFromRightmostBit);

        // Zero out those bits first
        bitArray &= ~setterMask;

        // Then OR with the value
        bitArray |= ((value<<offsetFromRightmostBit) & setterMask);
        return bitArray;
    }

    /**
     * Given a 16-bit short, generates a binary string
     * @param word
     * @return String of 1s and 0s showing binary layout of memory word
     */
    public static String wordToString(short word){
        String binaryString = Integer.toBinaryString(Short.toUnsignedInt(word));
        return String.format("%1$16s", binaryString).replace(' ', '0');
    }

    public static short stringToWord(String string){
        return (short)Integer.parseUnsignedInt(string,2);
    }
}
