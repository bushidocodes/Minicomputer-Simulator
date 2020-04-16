package com.simulator.awesome;

public class FloatingPointNumber{
    public short sign;
    public short exponentSign;
    public short exponentValue;
    public short mantissa;

    FloatingPointNumber(short word) {
        short signMask          = (short) 0b1000000000000000;
        short exponentSignMask  = (short) 0b0100000000000000;
        short exponentValueMask = (short) 0b0011111100000000;
        short mantissaMask      = (short) 0b0000000011111111;

        short mantissaOffset      = 0;
        short exponentValueOffset = 8;
        short exponentSignOffset  = 14;
        short signOffset          = 15;

        this.sign = Utils.short_unsigned_right_shift((short) (word & signMask), signOffset);
        this.exponentSign = Utils.short_unsigned_right_shift((short) (word & exponentSignMask), exponentSignOffset);
        this.exponentValue = Utils.short_unsigned_right_shift((short) (word & exponentValueMask), exponentValueOffset);
        this.mantissa = Utils.short_unsigned_right_shift((short) (word & mantissaMask), mantissaOffset);
    }

    public short toShort(){
        String sign = String.valueOf(this.sign);
        String exponentSign = String.valueOf(this.exponentSign);
        String exponentValue = "0000000".substring(Integer.toBinaryString(this.exponentValue).length()) + Integer.toBinaryString(this.exponentValue); // pad to 7 bits
        String mantissa = Integer.toBinaryString(this.mantissa);
        short composedShort = (short) Integer.parseUnsignedInt(sign + exponentSign + exponentValue + mantissa, 2);
        return composedShort;
    }
}