package com.simulator.awesome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArithmeticLogicUnitTest {

    private Simulator sim;
    private ArithmeticLogicUnit alu;

    @BeforeEach
    void setUp() {
        sim = new Simulator();
        alu = sim.alu;
    }

    // --- add ---

    @Test
    void add_simpleSum() {
        alu.setA((short) 3);
        alu.setB((short) 4);
        alu.add();
        assertEquals((short) 7, alu.getYAsShort());
    }

    @Test
    void add_equal_setsEqualFlag() {
        alu.setA((short) 5);
        alu.setB((short) 5);
        alu.add();
        assertTrue(sim.cc.isEqual());
    }

    @Test
    void add_overflow_setsOverflowFlag() {
        // 0x8300 (33536 unsigned) + 0x8300 = 67072, overflows 16-bit unsigned range
        alu.setA((short) -32000); // 0x8300 = 33536 unsigned
        alu.setB((short) -32000);
        alu.add();
        assertTrue(sim.cc.isOverflow());
    }

    @Test
    void add_noOverflow_clearOverflowFlag() {
        alu.setA((short) 100);
        alu.setB((short) 200);
        alu.add();
        assertFalse(sim.cc.isOverflow());
    }

    // --- subtract ---

    @Test
    void subtract_simpleDifference() {
        alu.setA((short) 10);
        alu.setB((short) 3);
        alu.subtract();
        assertEquals((short) 7, alu.getYAsShort());
    }

    @Test
    void subtract_equal_setsEqualFlag() {
        alu.setA((short) 7);
        alu.setB((short) 7);
        alu.subtract();
        assertTrue(sim.cc.isEqual());
        assertEquals((short) 0, alu.getYAsShort());
    }

    @Test
    void subtract_underflow_setsUnderflowFlag() {
        alu.setA((short) 0);
        alu.setB((short) 1);
        alu.subtract();
        assertTrue(sim.cc.isUnderflow());
    }

    // --- compare ---

    @Test
    void compare_equal() {
        alu.setA((short) 42);
        alu.setB((short) 42);
        alu.compare();
        assertTrue(sim.cc.isEqual());
        assertFalse(sim.cc.isGreaterThan());
    }

    @Test
    void compare_greaterThan() {
        alu.setA((short) 10);
        alu.setB((short) 5);
        alu.compare();
        assertFalse(sim.cc.isEqual());
        assertTrue(sim.cc.isGreaterThan());
    }

    @Test
    void compare_lessThan() {
        alu.setA((short) 5);
        alu.setB((short) 10);
        alu.compare();
        assertFalse(sim.cc.isEqual());
        assertFalse(sim.cc.isGreaterThan());
    }

    // --- multiply ---

    @Test
    void multiply_simpleProduct() {
        alu.setA((short) 6);
        alu.setB((short) 7);
        alu.multiply();
        assertEquals((short) 42, alu.getYAsShort());
    }

    @Test
    void multiply_byZero() {
        alu.setA((short) 999);
        alu.setB((short) 0);
        alu.multiply();
        assertEquals((short) 0, alu.getYAsShort());
    }

    // --- divide ---

    @Test
    void divide_simpleQuotient() {
        alu.setA((short) 10);
        alu.setB((short) 3);
        alu.divide();
        assertEquals((short) 3, alu.getYAsShort());
        assertEquals((short) 1, alu.getY2AsShort());
    }

    @Test
    void divide_byZero_setsDivideByZeroFlag() {
        alu.setA((short) 10);
        alu.setB((short) 0);
        alu.divide();
        assertTrue(sim.cc.isDivideByZero());
    }

    @Test
    void divide_exact() {
        alu.setA((short) 20);
        alu.setB((short) 4);
        alu.divide();
        assertEquals((short) 5, alu.getYAsShort());
        assertEquals((short) 0, alu.getY2AsShort());
    }

    // --- and ---

    @Test
    void and_basic() {
        alu.setA((short) 0b1100);
        alu.setB((short) 0b1010);
        alu.and();
        assertEquals((short) 0b1000, alu.getYAsShort());
    }

    // --- or ---

    @Test
    void or_basic() {
        alu.setA((short) 0b1100);
        alu.setB((short) 0b1010);
        alu.or();
        assertEquals((short) 0b1110, alu.getYAsShort());
    }

    // --- not ---

    @Test
    void not_zero_givesAllOnes() {
        alu.setA((short) 0);
        alu.not();
        assertEquals((short) -1, alu.getYAsShort());
    }

    @Test
    void not_allOnes_givesZero() {
        alu.setA((short) -1);
        alu.not();
        assertEquals((short) 0, alu.getYAsShort());
    }

    // --- decrementAndCompare ---

    @Test
    void decrementAndCompare_equalsB_afterDecrement() {
        alu.setA((short) 6);
        alu.setB((short) 5);
        alu.decrementAndCompare();
        assertEquals((short) 5, alu.getYAsShort());
        assertTrue(sim.cc.isEqual());
    }

    @Test
    void decrementAndCompare_greaterThan_afterDecrement() {
        alu.setA((short) 10);
        alu.setB((short) 5);
        alu.decrementAndCompare();
        assertEquals((short) 9, alu.getYAsShort());
        assertTrue(sim.cc.isGreaterThan());
        assertFalse(sim.cc.isEqual());
    }

    // --- logical shifts ---

    @Test
    void logicalShiftRight_noSignExtension() {
        alu.setA((short) -32768); // 0x8000
        alu.setB((short) 1);
        alu.logicalShiftRight();
        assertEquals((short) 0x4000, alu.getYAsShort());
    }

    @Test
    void logicalShiftLeft_basic() {
        alu.setA((short) 1);
        alu.setB((short) 3);
        alu.logicalShiftLeft();
        assertEquals((short) 8, alu.getYAsShort());
    }

    // --- shift underflow flag (issue #108) ---

    @Test
    void arithmeticShiftRight_underflow_setBitsLost() {
        // 0b0000000000000011 >> 1 loses the bit at position 0
        alu.setA((short) 0b11);
        alu.setB((short) 1);
        alu.arithmeticShiftRight();
        assertTrue(sim.cc.isUnderflow(), "underflow should be set when 1-bits are shifted off");
    }

    @Test
    void arithmeticShiftRight_noUnderflow_noLostBits() {
        // 0b0000000000000100 >> 2 shifts out only zeros
        alu.setA((short) 0b100);
        alu.setB((short) 2);
        alu.arithmeticShiftRight();
        assertFalse(sim.cc.isUnderflow(), "underflow should be clear when only zero-bits are shifted off");
    }

    @Test
    void arithmeticShiftRight_noUnderflow_zeroShift() {
        alu.setA((short) 0b111);
        alu.setB((short) 0);
        alu.arithmeticShiftRight();
        assertFalse(sim.cc.isUnderflow(), "underflow should be clear for shift count 0");
    }

    @Test
    void logicalShiftRight_underflow_setBitsLost() {
        // 0b0000000000000011 >> 1 loses the bit at position 0
        alu.setA((short) 0b11);
        alu.setB((short) 1);
        alu.logicalShiftRight();
        assertTrue(sim.cc.isUnderflow(), "underflow should be set when 1-bits are shifted off");
    }

    @Test
    void logicalShiftRight_noUnderflow_highBitSource() {
        // 0x8000 >> 1 shifts out only zeros from the right end
        alu.setA((short) 0x8000);
        alu.setB((short) 1);
        alu.logicalShiftRight();
        assertFalse(sim.cc.isUnderflow(), "underflow should be clear when only zero-bits are shifted off");
        assertEquals((short) 0x4000, alu.getYAsShort());
    }

    @Test
    void logicalShiftRight_noUnderflow_zeroShift() {
        alu.setA((short) 0b111);
        alu.setB((short) 0);
        alu.logicalShiftRight();
        assertFalse(sim.cc.isUnderflow(), "underflow should be clear for shift count 0");
    }

    // --- arithmeticShiftRight: underflow flag and count=0 (issue #118) ---

    @Test
    void arithmeticShiftRight_count0_resultUnchanged() {
        alu.setA((short) 0b11110000);
        alu.setB((short) 0);
        alu.arithmeticShiftRight();
        assertEquals((short) 0b11110000, alu.getYAsShort(),
            "shift by 0 must not change the value");
    }

    @Test
    void arithmeticShiftRight_count0_underflowClear() {
        alu.setA((short) 0b111);
        alu.setB((short) 0);
        alu.arithmeticShiftRight();
        assertFalse(sim.cc.isUnderflow(),
            "shift count 0 must not set underflow");
    }

    @Test
    void arithmeticShiftRight_underflow_setWhenBitsLost() {
        // 0b11 >> 1 shifts out the lsb (1-bit lost) - underflow
        alu.setA((short) 0b11);
        alu.setB((short) 1);
        alu.arithmeticShiftRight();
        assertTrue(sim.cc.isUnderflow(),
            "underflow must be set when 1-bits are shifted off the right end");
    }

    @Test
    void arithmeticShiftRight_noUnderflow_onlyZerosBitsLost() {
        // 0b100 >> 2: bits shifted out are 0,0 - no underflow
        alu.setA((short) 0b100);
        alu.setB((short) 2);
        alu.arithmeticShiftRight();
        assertFalse(sim.cc.isUnderflow(),
            "underflow must be clear when only zero-bits are shifted off");
    }

    @Test
    void arithmeticShiftRight_negative_signExtends() {
        // Java >> sign-extends: -4 >> 1 = -2
        alu.setA((short) -4);
        alu.setB((short) 1);
        alu.arithmeticShiftRight();
        assertEquals((short) -2, alu.getYAsShort(),
            "arithmetic right shift must sign-extend negative values");
    }

    // --- logicalShiftRight: underflow flag and count=0 (issue #118) ---

    @Test
    void logicalShiftRight_count0_resultUnchanged() {
        alu.setA((short) 0b11110000);
        alu.setB((short) 0);
        alu.logicalShiftRight();
        assertEquals((short) 0b11110000, alu.getYAsShort(),
            "logical shift right by 0 must not change the value");
    }

    @Test
    void logicalShiftRight_count0_underflowClear() {
        alu.setA((short) 0b111);
        alu.setB((short) 0);
        alu.logicalShiftRight();
        assertFalse(sim.cc.isUnderflow(),
            "shift count 0 must not set underflow flag");
    }

    @Test
    void logicalShiftRight_underflow_setWhenBitsLost() {
        alu.setA((short) 0b11);
        alu.setB((short) 1);
        alu.logicalShiftRight();
        assertTrue(sim.cc.isUnderflow(),
            "underflow must be set when 1-bits are shifted off the right end");
    }

    @Test
    void logicalShiftRight_noUnderflow_highBitSourceNoLoss() {
        // 0x8000 >> 1: right end bit is 0, no underflow; result = 0x4000
        alu.setA((short) 0x8000);
        alu.setB((short) 1);
        alu.logicalShiftRight();
        assertFalse(sim.cc.isUnderflow(),
            "underflow must be clear when the shifted-out bits are all zero");
        assertEquals((short) 0x4000, alu.getYAsShort());
    }

    @Test
    void logicalShiftRight_doesNotSignExtend() {
        // Logical shift: sign bit must NOT propagate
        alu.setA((short) 0x8000);
        alu.setB((short) 1);
        alu.logicalShiftRight();
        assertEquals((short) 0x4000, alu.getYAsShort(),
            "logical right shift must fill with 0, not sign-extend");
    }

    // --- arithmeticShiftLeft: count=0 (issue #118) ---

    @Test
    void arithmeticShiftLeft_count0_resultUnchanged() {
        alu.setA((short) 0b10101010);
        alu.setB((short) 0);
        alu.arithmeticShiftLeft();
        assertEquals((short) 0b10101010, alu.getYAsShort(),
            "shift left by 0 must not change the value");
    }

    // --- logical rotates: count=0 (issue #118) ---

    @Test
    void logicalRotateLeft_count0_resultUnchanged() {
        alu.setA((short) 0b1010101010101010);
        alu.setB((short) 0);
        alu.logicalRotateLeft();
        assertEquals((short) 0b1010101010101010, alu.getYAsShort(),
            "rotate left by 0 must not change the value");
    }

    @Test
    void logicalRotateRight_count0_resultUnchanged() {
        alu.setA((short) 0b0101010101010101);
        alu.setB((short) 0);
        alu.logicalRotateRight();
        assertEquals((short) 0b0101010101010101, alu.getYAsShort(),
            "rotate right by 0 must not change the value");
    }

    @Test
    void logicalRotateLeft_wrapsHighBitToLow() {
        // 0x8000 rotate-left by 1: high bit wraps to bit 0 - 0x0001
        alu.setA((short) 0x8000);
        alu.setB((short) 1);
        alu.logicalRotateLeft();
        assertEquals((short) 0x0001, alu.getYAsShort(),
            "logical rotate left must wrap the high bit around to position 0");
    }

    @Test
    void logicalRotateRight_wrapsLowBitToHigh() {
        // 0x0001 rotate-right by 1: low bit wraps to bit 15 - 0x8000
        alu.setA((short) 0x0001);
        alu.setB((short) 1);
        alu.logicalRotateRight();
        assertEquals((short) 0x8000, alu.getYAsShort(),
            "logical rotate right must wrap the low bit around to position 15");
    }

    // --- arithmeticShiftRight underflow was broken (issue #108, fixed) ---
    // These document the correct behavior after the fix.

    @Test
    void arithmeticShiftRight_underflowUsesLostBits_notModulo10() {
        // Before fix: `a % 10 != 0` was used -- value 10 would give no underflow incorrectly.
        // After fix: check whether the bits shifted off the right are non-zero.
        alu.setA((short) 10); // 10 % 10 == 0, so old code said no underflow
        alu.setB((short) 1);  // shift out bit 0: 10 = 0b1010, bit 0 = 0 - no underflow
        alu.arithmeticShiftRight();
        assertFalse(sim.cc.isUnderflow(),
            "10 >> 1: bit 0 is 0, so no bits are lost -- underflow must be clear");
    }

    @Test
    void arithmeticShiftRight_underflowBitLoss_oddValue() {
        // 11 >> 1: 11 = 0b1011, bit 0 = 1 - underflow (bit lost)
        alu.setA((short) 11);
        alu.setB((short) 1);
        alu.arithmeticShiftRight();
        assertTrue(sim.cc.isUnderflow(),
            "11 >> 1: bit 0 is 1, a 1-bit is lost -- underflow must be set");
    }
}
