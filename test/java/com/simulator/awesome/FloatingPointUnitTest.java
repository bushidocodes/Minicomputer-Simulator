package com.simulator.awesome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FloatingPointUnit.
 *
 * FP word layout: S(1) | ExpSign(1) | ExpValue(6) | Mantissa(8) = 16 bits
 */
class FloatingPointUnitTest {

    private Simulator sim;
    private FloatingPointUnit fpu;

    /** Build a 16-bit FP word from its constituent fields. */
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
        fpu.setA(fp(0, 0, 1, 0));
        fpu.setB(fp(0, 0, 1, 0));
        fpu.add();
        assertFalse(sim.cc.isUnderflow(), "zero mantissa result should not set underflow");
    }

    @Test
    void add_nonzeroMantissa_setsUnderflow() {
        fpu.setA(fp(0, 0, 1, 0x05));
        fpu.setB(fp(0, 0, 1, 0x03));
        fpu.add();
        assertTrue(sim.cc.isUnderflow(),
            "non-zero mantissa result should set underflow (mantissa > FP_MANTISSA_MIN_VALUE)");
    }

    // --- setA / setB / getYAsShort (issue #117) ---

    @Test
    void setA_storesValue() {
        fpu.setA(fp(0, 0, 2, 0x10));
        fpu.setB(fp(0, 0, 2, 0));
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(0x10, Short.toUnsignedInt(result.mantissa));
    }

    @Test
    void initialState_getYAsShort_isZero() {
        assertEquals((short) 0, fpu.getYAsShort(),
            "FPU output must be zero before any operation");
    }

    // --- add (issue #117) ---

    @Test
    void add_twoPositiveMantissas_sameExponent() {
        fpu.setA(fp(0, 0, 2, 0x10));
        fpu.setB(fp(0, 0, 2, 0x08));
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(0x18, Short.toUnsignedInt(result.mantissa));
    }

    @Test
    void add_zeroToZero_givesZero() {
        fpu.setA(fp(0, 0, 0, 0));
        fpu.setB(fp(0, 0, 0, 0));
        fpu.add();
        assertEquals((short) 0, fpu.getYAsShort());
    }

    @Test
    void add_differentExponents_rewritesSmaller() {
        // A: expValue=2, B: expValue=4; A.mantissa 0x10 >> 2 = 0x04; result = 0x14
        fpu.setA(fp(0, 0, 2, 0x10));
        fpu.setB(fp(0, 0, 4, 0x10));
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(0x14, Short.toUnsignedInt(result.mantissa),
            "when exponents differ, smaller mantissa is right-shifted to align");
    }

    // --- rewriteExponents sets resultExponentValue (issue #140) ---

    @Test
    void add_resultExponentTakesLargerOperandsExponent() {
        // B has the larger exponent value (4 > 2) -- result exponent should be 4
        fpu.setA(fp(0, 0, 2, 0x10));
        fpu.setB(fp(0, 0, 4, 0x10));
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(4, result.exponentValue,
            "result exponent must match the larger of the two operand exponents");
    }

    @Test
    void add_mantissaOverflow_shiftsAndIncrementsExponent() {
        // 0x80 + 0x80 = 0x100 > FP_MANTISSA_MAX (0xFF): mantissa shifted right, exponent incremented
        fpu.setA(fp(0, 0, 2, 0x80));
        fpu.setB(fp(0, 0, 2, 0x80));
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertTrue(Short.toUnsignedInt(result.mantissa) <= Config.FP_MANTISSA_MAX_VALUE,
            "overflow mantissa must be shifted to fit in 8 bits");
        assertEquals(3, result.exponentValue,
            "exponent must be incremented when mantissa overflows (base 2 + 1 overflow = 3)");
    }

    @Test
    void add_resultExponentSign_fromBExponent_notBSign() {
        // Distinct values from the #109 test above
        fpu.setA(fp(0, 1, 1, 0x08));
        fpu.setB(fp(1, 0, 3, 0x04));
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(0, result.exponentSign,
            "resultExponentSign must come from B.exponentSign (0), not B.sign (1)");
    }

    @Test
    void add_resultExponentSign_fromAExponent_notASign() {
        fpu.setA(fp(1, 0, 3, 0x04));
        fpu.setB(fp(0, 1, 1, 0x08));
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(0, result.exponentSign,
            "resultExponentSign must come from A.exponentSign (0), not A.sign (1)");
    }

    @Test
    void add_mantissaOverflow_fitsIn8Bits() {
        // 0x80 + 0x80 = 0x100 > FP_MANTISSA_MAX (0xFF): mantissa must be shifted to fit
        fpu.setA(fp(0, 0, 2, 0x80));
        fpu.setB(fp(0, 0, 2, 0x80));
        fpu.add();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertTrue(result.mantissa <= Config.FP_MANTISSA_MAX_VALUE,
            "overflow mantissa must be shifted to fit in 8 bits");
    }

    @Test
    void add_nonzeroMantissa_setsUnderflowFlag() {
        fpu.setA(fp(0, 0, 1, 0x05));
        fpu.setB(fp(0, 0, 1, 0x03));
        fpu.add();
        assertTrue(sim.cc.isUnderflow(),
            "non-zero mantissa (> FP_MANTISSA_MIN_VALUE=0) sets underflow flag");
    }

    // --- setFixed / getFixed / setConversionType (issue #117) ---

    @Test
    void setFixed_getFixed_roundTrips() {
        fpu.setFixed((short) 42);
        assertEquals((short) 42, fpu.getFixed());
    }

    @Test
    void setConversionType_acceptsZero() {
        fpu.setConversionType(0);
        assertEquals(0, fpu.getConversionType());
    }

    @Test
    void setConversionType_acceptsOne() {
        fpu.setConversionType(1);
        assertEquals(1, fpu.getConversionType());
    }

    @Test
    void setConversionType_ignoresInvalidValue() {
        fpu.setConversionType(0);
        fpu.setConversionType(99);
        assertEquals(0, fpu.getConversionType());
    }

    // --- convert (issue #117) ---

    @Test
    void convert_fixedToFloat_positiveValue_signIsZero() {
        fpu.setFixed((short) 128);
        fpu.setConversionType(1);
        fpu.convert();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(0, result.sign, "positive fixed-point value must produce sign=0");
    }

    @Test
    void convert_fixedToFloat_integerValue_hasPositiveExponent() {
        fpu.setFixed((short) 256);
        fpu.setConversionType(1);
        fpu.convert();
        FloatingPointNumber result = new FloatingPointNumber(fpu.getYAsShort());
        assertEquals(0, result.exponentSign,
            "value >= 256 should produce a positive (non-negative) exponent");
    }

    @Test
    void convert_floatToFixed_negativeExponent_shiftsRight() {
        // sign=0, expSign=1 (negative exponent), expValue=2, mantissa=0x80
        // Negative exponent: shift mantissa right by 2 -> 0x20
        fpu.setA(fp(0, 1, 2, 0x80));
        fpu.setConversionType(0);
        fpu.convert();
        assertEquals(0x20, fpu.getFixed() & 0xFF,
            "negative exponent should right-shift mantissa by exponent value");
    }
}
