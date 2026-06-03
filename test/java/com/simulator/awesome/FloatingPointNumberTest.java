package com.simulator.awesome;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FloatingPointNumberTest {

    // FP word layout: S(1) | ExpSign(1) | ExpValue(6) | Mantissa(8) = 16 bits
    private static short fp(int sign, int expSign, int expValue, int mantissa) {
        return (short) ((sign << 15) | (expSign << 14) | (expValue << 8) | mantissa);
    }

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
        // Compose a value with all field bits set and verify it fits in 16 bits
        short word = fp(1, 1, 63, 0xFF);
        // fp(1,1,63,255) = 0xFFFF — if exponent were padded to 7 bits the parse would fail
        // or produce wrong bits. The key check: no exception and correct value.
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
}
