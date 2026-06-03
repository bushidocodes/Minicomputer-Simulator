package com.simulator.awesome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FloatingPointNumber.
 *
 * FP word layout: S(1) | ExpSign(1) | ExpValue(6) | Mantissa(8) = 16 bits
 * Bit positions:  15     14           13-8           7-0
 */
class FloatingPointNumberTest {

    /** Build a 16-bit FP word from its constituent fields. */
    private static short fp(int sign, int expSign, int expValue, int mantissa) {
        return (short) ((sign << 15) | (expSign << 14) | (expValue << 8) | mantissa);
    }

    // --- Constructor: field extraction (issue #117) ---

    @Test
    void constructor_zero_allFieldsZero() {
        FloatingPointNumber n = new FloatingPointNumber((short) 0);
        assertEquals(0, n.sign);
        assertEquals(0, n.exponentSign);
        assertEquals(0, n.exponentValue);
        assertEquals(0, Short.toUnsignedInt(n.mantissa));
    }

    @Test
    void constructor_signBit_extracted() {
        short word = fp(1, 0, 0, 0);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(1, n.sign);
    }

    @Test
    void constructor_exponentSignBit_extracted() {
        short word = fp(0, 1, 0, 0);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(1, n.exponentSign);
    }

    @Test
    void constructor_exponentValue_extracted() {
        // exponentValue = 0b101010 = 42, stored in bits 13-8
        short word = fp(0, 0, 42, 0);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(42, n.exponentValue);
    }

    @Test
    void constructor_maxExponentValue_extracted() {
        // 6-bit max = 63 = 0b111111
        short word = fp(0, 0, 63, 0);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(63, n.exponentValue);
    }

    @Test
    void constructor_mantissa_extracted() {
        // mantissa = 0xAB = 171
        short word = fp(0, 0, 0, 0xAB);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(0xAB, Short.toUnsignedInt(n.mantissa));
    }

    @Test
    void constructor_maxMantissa_extracted() {
        // mantissa = 0xFF = 255
        short word = fp(0, 0, 0, 0xFF);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(0xFF, Short.toUnsignedInt(n.mantissa));
    }

    @Test
    void constructor_allFields_extracted() {
        // sign=1, expSign=0, expValue=5, mantissa=0x3C
        short word = fp(1, 0, 5, 0x3C);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(1, n.sign);
        assertEquals(0, n.exponentSign);
        assertEquals(5, n.exponentValue);
        assertEquals(0x3C, Short.toUnsignedInt(n.mantissa));
    }

    @Test
    void constructor_allBitsSet_allFieldsMaximal() {
        // 0xFFFF = fp(1, 1, 63, 255)
        FloatingPointNumber n = new FloatingPointNumber((short) 0xFFFF);
        assertEquals(1, n.sign);
        assertEquals(1, n.exponentSign);
        assertEquals(63, n.exponentValue);
        assertEquals(255, Short.toUnsignedInt(n.mantissa));
    }

    // --- toShort(): round-trip (issue #111 / #117) ---

    @Test
    void roundTrip_zero() {
        short word = fp(0, 0, 0, 0);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(word, n.toShort(), "zero round-trips correctly");
    }

    @Test
    void roundTrip_maxMantissa() {
        // mantissa = 0xFF (8 bits all set), exponentValue = 0, expSign = 0, sign = 0
        short word = fp(0, 0, 0, 0xFF);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(word, n.toShort(), "max mantissa round-trips correctly");
    }

    @Test
    void roundTrip_maxExponent() {
        // exponentValue = 63 = 0b111111 (6 bits all set)
        short word = fp(0, 0, 63, 0);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(word, n.toShort(), "max exponent (6 bits) round-trips correctly");
    }

    @Test
    void roundTrip_allBitsSet() {
        // sign=1, expSign=1, expValue=63, mantissa=0xFF
        short word = fp(1, 1, 63, 0xFF);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(word, n.toShort(), "all-bits-set round-trips correctly");
    }

    @Test
    void roundTrip_typicalValue() {
        // sign=0, expSign=0, exponent=4, mantissa=0x8F (1000_1111)
        short word = fp(0, 0, 4, 0x8F);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(word, n.toShort(), "typical value round-trips correctly");
    }

    @Test
    void toShort_totalLength_is16bits() {
        // Compose a value with all field bits set and verify it fits in 16 bits.
        // fp(1,1,63,255) = 0xFFFF -- if exponent were padded to 7 bits this would fail.
        short word = fp(1, 1, 63, 0xFF);
        assertEquals((short) 0xFFFF, word);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(word, n.toShort());
    }

    @Test
    void decompose_fields_areCorrect() {
        short word = fp(1, 0, 5, 0xAB);
        FloatingPointNumber n = new FloatingPointNumber(word);
        assertEquals(1, n.sign);
        assertEquals(0, n.exponentSign);
        assertEquals(5, n.exponentValue);
        assertEquals(0xAB, Short.toUnsignedInt(n.mantissa));
    }

    @Test
    void toShort_zero_roundTrips() {
        short word = fp(0, 0, 0, 0);
        assertEquals(word, new FloatingPointNumber(word).toShort());
    }

    @Test
    void toShort_maxMantissa_roundTrips() {
        short word = fp(0, 0, 0, 0xFF);
        assertEquals(word, new FloatingPointNumber(word).toShort());
    }

    @Test
    void toShort_maxExponent_roundTrips() {
        short word = fp(0, 0, 63, 0);
        assertEquals(word, new FloatingPointNumber(word).toShort(),
            "6-bit max exponent (63) must round-trip");
    }

    @Test
    void toShort_allBitsSet_roundTrips() {
        short word = fp(1, 1, 63, 0xFF);
        assertEquals(word, new FloatingPointNumber(word).toShort());
    }

    @Test
    void toShort_typicalValue_roundTrips() {
        // sign=0, expSign=0, exponent=4, mantissa=0x8F (10001111)
        short word = fp(0, 0, 4, 0x8F);
        assertEquals(word, new FloatingPointNumber(word).toShort());
    }

    @Test
    void toShort_signedNegative_roundTrips() {
        // sign=1, expSign=1, expValue=7, mantissa=0x42
        short word = fp(1, 1, 7, 0x42);
        assertEquals(word, new FloatingPointNumber(word).toShort());
    }

    @Test
    void toShort_mantissaWithLeadingZeros_roundTrips() {
        // mantissa = 0x01 has leading zeros; must still be padded to 8 bits
        short word = fp(0, 0, 3, 0x01);
        assertEquals(word, new FloatingPointNumber(word).toShort(),
            "mantissa with leading zeros must be padded to 8 bits in toShort()");
    }

    @Test
    void toShort_exponentWithLeadingZeros_roundTrips() {
        // exponentValue = 1 = 0b000001 in 6 bits; must still produce correct word
        short word = fp(0, 0, 1, 0x80);
        assertEquals(word, new FloatingPointNumber(word).toShort(),
            "exponent with leading zeros must be padded to 6 bits in toShort()");
    }
}
