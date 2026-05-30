package com.simulator.awesome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    // --- getNthLeastSignificantBit ---

    @Test
    void getNthLeastSignificantBit_bit0_set() {
        assertTrue(Utils.getNthLeastSignificantBit(0b0001, 0));
    }

    @Test
    void getNthLeastSignificantBit_bit0_clear() {
        assertFalse(Utils.getNthLeastSignificantBit(0b0000, 0));
    }

    @Test
    void getNthLeastSignificantBit_bit3_set() {
        assertTrue(Utils.getNthLeastSignificantBit(0b1000, 3));
    }

    @Test
    void getNthLeastSignificantBit_bit3_clear() {
        assertFalse(Utils.getNthLeastSignificantBit(0b0100, 3));
    }

    // --- setNthLeastSignificantBit ---

    @Test
    void setNthLeastSignificantBit_setBit0() {
        short result = Utils.setNthLeastSignificantBit((short) 0, 0, true);
        assertEquals((short) 1, result);
    }

    @Test
    void setNthLeastSignificantBit_clearBit0() {
        short result = Utils.setNthLeastSignificantBit((short) 0b0001, 0, false);
        assertEquals((short) 0, result);
    }

    @Test
    void setNthLeastSignificantBit_setBit3() {
        short result = Utils.setNthLeastSignificantBit((short) 0, 3, true);
        assertEquals((short) 0b1000, result);
    }

    @Test
    void setNthLeastSignificantBit_preservesOtherBits() {
        short result = Utils.setNthLeastSignificantBit((short) 0b1010, 0, true);
        assertEquals((short) 0b1011, result);
    }

    // --- getNthLeastSignificantBits ---

    @Test
    void getNthLeastSignificantBits_lowestByte() {
        // Extract bits [7:0] from 0x00FF -> should be 0xFF = 255
        short result = Utils.getNthLeastSignificantBits(0x00FF, 0, 8);
        assertEquals((short) 0xFF, result);
    }

    @Test
    void getNthLeastSignificantBits_upperBits() {
        // Extract bits [15:8] from 0xFF00 -> should be 0xFF = 255
        short result = Utils.getNthLeastSignificantBits(0xFF00, 8, 8);
        assertEquals((short) 0xFF, result);
    }

    @Test
    void getNthLeastSignificantBits_3bitField() {
        // 0b00110_11 -> bits [4:2] = 0b110 = 6
        short result = Utils.getNthLeastSignificantBits(0b0011011, 2, 3);
        assertEquals((short) 6, result);
    }

    @Test
    void getNthLeastSignificantBits_singleBit() {
        short result = Utils.getNthLeastSignificantBits(0b100, 2, 1);
        assertEquals((short) 1, result);
    }

    // --- setNthLeastSignificantBits ---

    @Test
    void setNthLeastSignificantBits_writeLowByte() {
        short result = Utils.setNthLeastSignificantBits((short) 0, 0, 8, (short) 0xAB);
        assertEquals((short) 0xAB, result);
    }

    @Test
    void setNthLeastSignificantBits_preservesOtherBits() {
        // Start with 0xFF00, write 0x42 into bits [7:0]
        short result = Utils.setNthLeastSignificantBits((short) 0xFF00, 0, 8, (short) 0x42);
        assertEquals((short) 0xFF42, result);
    }

    @Test
    void setNthLeastSignificantBits_roundTrip() {
        short value = (short) 0b101;
        short packed = Utils.setNthLeastSignificantBits((short) 0, 3, 3, value);
        short unpacked = Utils.getNthLeastSignificantBits(packed, 3, 3);
        assertEquals(value, unpacked);
    }

    // --- wordToString / stringToWord ---

    @Test
    void wordToString_zero() {
        assertEquals("0000000000000000", Utils.wordToString((short) 0));
    }

    @Test
    void wordToString_one() {
        assertEquals("0000000000000001", Utils.wordToString((short) 1));
    }

    @Test
    void wordToString_maxUnsigned() {
        assertEquals("1111111111111111", Utils.wordToString((short) -1));
    }

    @Test
    void stringToWord_zero() {
        assertEquals((short) 0, Utils.stringToWord("0000000000000000"));
    }

    @Test
    void stringToWord_one() {
        assertEquals((short) 1, Utils.stringToWord("0000000000000001"));
    }

    @Test
    void wordToString_stringToWord_roundTrip() {
        short original = (short) 0b0101010101010101;
        assertEquals(original, Utils.stringToWord(Utils.wordToString(original)));
    }

    // --- short_unsigned_right_shift ---

    @Test
    void shortUnsignedRightShift_positiveValue() {
        // 0b0000000000001000 >> 2 == 0b0000000000000010
        short result = Utils.short_unsigned_right_shift((short) 8, 2);
        assertEquals((short) 2, result);
    }

    @Test
    void shortUnsignedRightShift_noSignExtension() {
        // 0b1000000000000000 (-32768) unsigned right shift by 1 must not sign-extend
        short result = Utils.short_unsigned_right_shift((short) -32768, 1);
        // Unsigned: 0x8000 >> 1 = 0x4000 = 16384
        assertEquals((short) 0x4000, result);
    }

    @Test
    void shortUnsignedRightShift_byZero() {
        short result = Utils.short_unsigned_right_shift((short) 0xAB, 0);
        assertEquals((short) 0xAB, result);
    }
}
