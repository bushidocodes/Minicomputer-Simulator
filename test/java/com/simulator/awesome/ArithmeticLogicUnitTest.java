package com.simulator.awesome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArithmeticLogicUnitTest {

    private Simulator sim;
    private ArithmeticLogicUnit alu;

    @BeforeEach
    void setUp() {
        sim = new Simulator(Config.WORD_COUNT);
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
}
