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
        // A: sign=0, expSign=0 (positive exp), expValue=2, mantissa=0x10
        // B: sign=1, expSign=1 (negative exp), expValue=4, mantissa=0x10
        // B has the larger exponent value, so resultExponentSign should come from B.exponentSign (=1),
        // NOT from B.sign (=1). This test would also pass by accident if sign==expSign, so we
        // use a case where they differ for A (sign=0, expSign=1) vs B (sign=1, expSign=0).
        //
        // A: sign=0, expSign=1, expValue=2, mantissa=0x10  (positive number, negative exponent=2)
        // B: sign=1, expSign=0, expValue=4, mantissa=0x08  (negative number, positive exponent=4)
        // B.exponentValue > A.exponentValue → resultExponentSign should be B.exponentSign = 0
        short a = fp(0, 1, 2, 0x10);
        short b = fp(1, 0, 4, 0x08);
        fpu.setA(a);
        fpu.setB(b);
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        // resultExponentSign should be B.exponentSign = 0 (positive exponent)
        assertEquals(0, result.exponentSign,
            "resultExponentSign must come from B.exponentSign (0), not B.sign (1)");
    }

    @Test
    void add_resultExponentSign_takesAFromExponentSign_notOverallSign() {
        // A: sign=1, expSign=0, expValue=4, mantissa=0x10  (negative number, positive exp=4)
        // B: sign=0, expSign=1, expValue=2, mantissa=0x08  (positive number, negative exp=2)
        // A.exponentValue > B.exponentValue → resultExponentSign should be A.exponentSign = 0
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
        // A: sign=1, expSign=0, expValue=3, mantissa=0x10
        // B: sign=0, expSign=1, expValue=3, mantissa=0x08
        // same exponent value → resultExponentSign taken from A.exponentSign = 0
        short a = fp(1, 0, 3, 0x10);
        short b = fp(0, 1, 3, 0x08);
        fpu.setA(a);
        fpu.setB(b);
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(0, result.exponentSign,
            "when exponents are equal, resultExponentSign must come from A.exponentSign (0), not A.sign (1)");
    }
}
