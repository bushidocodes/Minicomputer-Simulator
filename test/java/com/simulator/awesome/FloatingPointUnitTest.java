package com.simulator.awesome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FloatingPointUnitTest {

    private Simulator sim;
    private FloatingPointUnit fpu;

    // FP word layout: S(1) | ExpSign(1) | ExpValue(6) | Mantissa(8)
    // Bit positions:  15     14           13-8           7-0
    private static short fp(int sign, int expSign, int expValue, int mantissa) {
        return (short) ((sign << 15) | (expSign << 14) | (expValue << 8) | mantissa);
    }

    @BeforeEach
    void setUp() {
        sim = new Simulator(Config.WORD_COUNT);
        fpu = sim.fpu;
    }

    // --- rewriteExponents preserves exponent sign, not overall sign (issue #109) ---

    @Test
    void add_resultExponentSign_takesBFromExponentSign_notOverallSign() {
        // A: sign=0, expSign=1, expValue=2; B: sign=1, expSign=0, expValue=4 (larger)
        // B.exponentValue > A.exponentValue -> resultExponentSign should be B.exponentSign = 0
        short a = fp(0, 1, 2, 0x10);
        short b = fp(1, 0, 4, 0x08);
        fpu.setA(a);
        fpu.setB(b);
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(0, result.exponentSign,
            "resultExponentSign must come from B.exponentSign (0), not B.sign (1)");
    }

    @Test
    void add_resultExponentSign_takesAFromExponentSign_notOverallSign() {
        // A: sign=1, expSign=0, expValue=4 (larger); B: sign=0, expSign=1, expValue=2
        // A.exponentValue > B.exponentValue -> resultExponentSign should be A.exponentSign = 0
        short a = fp(1, 0, 4, 0x10);
        short b = fp(0, 1, 2, 0x08);
        fpu.setA(a);
        fpu.setB(b);
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(0, result.exponentSign,
            "resultExponentSign must come from A.exponentSign (0), not A.sign (1)");
    }

    @Test
    void add_sameExponent_takesAExponentSign() {
        // A: sign=1, expSign=0, expValue=3; B: sign=0, expSign=1, expValue=3 (equal)
        // same exponent value -> resultExponentSign taken from A.exponentSign = 0
        short a = fp(1, 0, 3, 0x10);
        short b = fp(0, 1, 3, 0x08);
        fpu.setA(a);
        fpu.setB(b);
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(0, result.exponentSign,
            "when exponents are equal, resultExponentSign must come from A.exponentSign (0), not A.sign (1)");
    }

    // --- composeResult calls setUnderflow only once (issue #110) ---

    @Test
    void add_zeroMantissaResult_doesNotSetUnderflow() {
        // Two zero-mantissa operands -> mantissaUnderflow = false, underflow flag = false.
        short a = fp(0, 0, 1, 0);
        short b = fp(0, 0, 1, 0);
        fpu.setA(a);
        fpu.setB(b);
        fpu.add();
        assertFalse(sim.cc.isUnderflow(), "zero mantissa result should not set underflow");
    }

    @Test
    void add_nonzeroMantissa_setsUnderflow() {
        // FP_MANTISSA_MIN_VALUE=0, so mantissa > 0 triggers underflow.
        short a = fp(0, 0, 1, 0x05);
        short b = fp(0, 0, 1, 0x03);
        fpu.setA(a);
        fpu.setB(b);
        fpu.add();
        assertTrue(sim.cc.isUnderflow(),
            "non-zero mantissa result should set underflow (mantissa > FP_MANTISSA_MIN_VALUE)");
    }
}
