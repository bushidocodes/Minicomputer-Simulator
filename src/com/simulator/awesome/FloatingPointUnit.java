package com.simulator.awesome;

/* General overview:
    1. Rewrite the smaller number so that its exponent matches with the exponent of the larger number.
    2. If either mantissa is negative, convert it to 2's complement.
    3. For addition, add the mantissas. For subtraction, subtract the mantissas.
    4. If the result mantissa is negative, convert it back to signed magnitude and set the sign bit appropriately.
    5. Try to handle overflow. If we can't, set overflow.
    6. Set the result to y.
*/

import static com.simulator.awesome.Utils.setNthLeastSignificantBit;
import static java.lang.Math.round;

public class FloatingPointUnit {

    // a and be are input registers for the FPU
    private FloatingPointNumber a;
    private FloatingPointNumber b;

    // y is the output register for the FPU
    private FloatingPointNumber y;

    // Used for composing the result before loading it into y
    int absoluteExponentDifference;
    short resultSign;
    short resultExponentSign;
    short resultExponentValue;
    short resultMantissa;

    // fixed is used for the fixed<->floating conversion
    private int fixed;

    // Type of conversion
    // 0 - Floating -> Fixed
    // 1 - Fixed -> Floating
    private int conversionType;

    private final Simulator context;

    FloatingPointUnit(Simulator context) {
        this.a = new FloatingPointNumber((short) 0);
        this.b = new FloatingPointNumber((short) 0);
        this.y = new FloatingPointNumber((short) 0);
        this.context = context;
    }

    public void setA(short a) {
        this.a = new FloatingPointNumber(a);
    }

    public void setB(short b) {
        this.b = new FloatingPointNumber(b);
    }

    public void setFixed(short fixed) { this.fixed = fixed; }

    public short getFixed() { return (short) this.fixed; };

    public short getYAsShort() {
        return this.y.toShort();
    }

    public void setConversionType(int value){
        // 0 = Floating -> Fixed
        // 1 = Fixed -> Floating
        if (value == 0 || value == 1) {
            conversionType = value;
        }
    }

    public int getConversionType(){
        return conversionType;
    }

    public void add(){
        rewriteExponents();
        convertMantissaToTwosComplement();
        // Add the mantissas
        resultMantissa = (short) (this.a.mantissa + this.b.mantissa);

        convertMantissaToSignedMagnitude();

        // Try to handle mantissa overflow by shifting and adjusting the exponent
        if (resultMantissa > Config.FP_MANTISSA_MAX_VALUE) {
            while (resultMantissa > Config.FP_MANTISSA_MAX_VALUE && resultExponentValue <= Config.FP_EXPONENT_MAX_VALUE){
                // Right shift mantissa by 1 bit and increment the exponent until it fits or we overflow
                resultMantissa = Utils.short_unsigned_right_shift(resultMantissa, 1);
                resultExponentValue++;
            }
        }
        composeResult();
    }

    public void subtract(){
        rewriteExponents();
        convertMantissaToTwosComplement();

        // Subtract the mantissas
        resultMantissa = (short) (this.a.mantissa - this.b.mantissa);

        convertMantissaToSignedMagnitude();

        // Try to handle mantissa overflow by shifting and adjusting the exponent
        if (resultMantissa > Config.FP_MANTISSA_MAX_VALUE) {
            while (resultMantissa > Config.FP_MANTISSA_MAX_VALUE && resultExponentValue <= Config.FP_EXPONENT_MAX_VALUE){
                // Right shift mantissa by 1 bit and increment the exponent until it fits or we overflow
                resultMantissa = Utils.short_unsigned_right_shift(resultMantissa, 1);
                resultExponentValue++;
            }
        }

        composeResult();
    }

    private void rewriteExponents(){
        // Rewrite the smaller number so that its exponent matches with the exponent of the larger number.

        if (this.a.exponentValue < this.b.exponentValue){
            // A is smaller than B
            absoluteExponentDifference = (this.b.exponentValue - this.a.exponentValue);

            // Shift A's mantissa to the right by the absoluteExponentDifference
            this.a.mantissa = Utils.short_unsigned_right_shift(this.a.mantissa, absoluteExponentDifference);

            // Add the absoluteExponentDifference to A's exponent
            this.a.exponentValue += absoluteExponentDifference;

            // The final exponent sign will be the exponent sign of B
            resultExponentSign = this.b.sign;

        } else if (this.a.exponentValue > this.b.exponentValue){
            // B is smaller than A
            absoluteExponentDifference = (this.a.exponentValue - this.b.exponentValue);

            // Shift B's mantissa to the right by the absoluteExponentDifference
            this.b.mantissa = Utils.short_unsigned_right_shift(this.b.mantissa, absoluteExponentDifference);

            // Add the absoluteExponentDifference to B's exponent
            this.b.exponentValue += absoluteExponentDifference;

            // The final exponent sign will be the exponent sign of A
            resultExponentSign = this.a.sign;
        } else {
            // the numbers have the same exponent already, so just pick one for the resultExponentSign
            resultExponentSign = this.a.sign;
        }
    }

    private void convertMantissaToTwosComplement(){
        // If either of the operands are negative, convert its mantissa to 2's complement
        if (this.a.sign == 1){
            // Invert the mantissa
            this.a.mantissa = (short) ~this.a.mantissa;
            // Add 1
            this.a.mantissa++;
        }
        if (this.b.sign == 1){
            // Invert the mantissa
            this.b.mantissa = (short) ~this.b.mantissa;
            // Add 1
            this.b.mantissa++;
        }
    }

    private void convertMantissaToSignedMagnitude(){
        // If the result is negative, convert the mantissa back to signed magnitude
        if (resultMantissa < 0) { // not sure if this negative check will work here
            resultMantissa = (short) ~resultMantissa;
            resultMantissa++;
            // Set the sign of the result
            resultSign = 1;
        } else {
            // Set the sign of the result
            resultSign = 0;
        }

        // Both exponents are the same, so just take one
        resultExponentValue = this.a.exponentValue;
    }

    private void composeResult(){
        // Set Condition Variables
        this.context.cc.reset();
        this.context.cc.setUnderflow(Integer.compareUnsigned(resultExponentValue, Config.FP_EXPONENT_MIN_VALUE) > 0);
        this.context.cc.setUnderflow(Integer.compareUnsigned(resultMantissa, Config.FP_MANTISSA_MIN_VALUE) > 0);

        // Compose the result
        this.y.sign = resultSign;
        this.y.exponentSign = resultExponentSign;
        this.y.exponentValue = resultExponentValue;
        this.y.mantissa = resultMantissa;
    }

    public void convert(){
        if (conversionType == 0){
            convertFloatingPointToFixedPoint();
        } else if (conversionType == 1){
            convertFixedPointToFloatingPoint();
        }
    }

    public void convertFloatingPointToFixedPoint(){
        /**
         * Given a floating point number
         *     0 | 0000000 | 00000000
         *     S | expnt   | mantissa
         *
         * Convert it to a fixed point number in the following format
         *     00000000 . 00000000
         *     integer    fraction
         *
         * Basic concept:
         *     1. Shift the decimal point in the mantissa n bits to the right (-) or left (+), where n is the value of the exponent
         *     2. Restore the assumed 1 that is to the left of the decimal point.
         *     3. Preserve the sign bit.
         **/

        // Store the input value into Y so it can be put into FR0
        this.y = this.a;

        // e.g. assume a number 0 0000100 10001111
         if (this.a.exponentSign == 1){
             // Negative exponent, shift right the mantissa by the exponent value.
             this.fixed = Utils.short_unsigned_right_shift(this.a.mantissa, this.a.exponentValue);
         } else {
             // Positive exponent, shift left the mantissa by the exponent value.
             // using the given number, mantissa is now 100011110000
             this.fixed = (this.a.mantissa <<= this.a.exponentValue);
        }

         // Restore the assumed 1, which should be immediately to the left of the most significant bit
         int leftMostBitDistance = getLeftMostSetBitDistance(this.a.mantissa);
         this.fixed = (this.fixed | (1 << (leftMostBitDistance + 1)));

        // Preserve the sign bit
        if(this.a.sign == 1){
            this.fixed = setNthLeastSignificantBit((short)this.fixed, 15,true);
        } else {
            this.fixed = setNthLeastSignificantBit((short)this.fixed, 15,false);
        }
    }

    public void convertFixedPointToFloatingPoint(){
        /**
         * Given a fixed point number in the following format
         *     00000000 . 00000000
         *     integer    fraction
         *
         * Convert it to a floating point number
         *     0 | 0000000 | 00000000
         *     S | expnt   | mantissa
         *
         * Basic concept:
         *     1. Find the most significant bit (left-most 1)
         *     2. Determine how far that bit is from the mid point. This distance is the exponent value.
         *     3. The mantissa becomes the maximum number of bits you can fit, starting from the most significant bit moving right (step 1)
         **/

        /**
         *  Get the number of places that the decimal point needs to be shifted from the center.
         *  It should be shifted so that the it is right after the most significant bit.
         *  The shifted distance is the value of value of the exponent
         *  Shifting left gives a positive exponent. Shifting right gives a negative exponent.
         *    e.g. given a number 00011000.11110000
         *         Shift it so we have 0001.100011110000
         *         The decimal was moved 4 places to the left, so the exponent is positive 4
         **/
        short decimalDistanceFromCenter;
        // if the integer bits are 0, then we need to move the decimal point to the right
        // in other words, if the short is between 00000000.00000000 and 00000000.11111111
        // 00000000.01100000
        if (this.fixed >= 0 && this.fixed < 256){
            decimalDistanceFromCenter = (short) (9-getLeftMostSetBitDistance(this.fixed));
            this.y.exponentValue = decimalDistanceFromCenter;
            this.y.exponentSign = 1;
        } else {
            // otherwise, shift the decimal to the left
            decimalDistanceFromCenter = (short) (getLeftMostSetBitDistance(this.fixed)-8);
            this.y.exponentValue = decimalDistanceFromCenter;
            this.y.exponentSign = 0;
        }

        // The sign of the number is the left-most bit.
        this.y.sign = Utils.short_unsigned_right_shift((short) this.fixed, 15);

        // Chop off the top insignificant bits (Lsh). This value is the mantissa.
        // The left-most 1 (the one to the left of the decimal point) is shifted off and assumed.
        // e.g. Given the result above      0001.100011110000
        //      Chop off bits until we get  .1000111100000000
        short temp;
        if(this.y.exponentSign == 1){
            temp = (short) (this.fixed << (8+decimalDistanceFromCenter));
        } else {
            temp = (short) (this.fixed << (8-decimalDistanceFromCenter));
        }
        // Chop off the right bits of the mantissa until it fits within 8 bits.
        // e.g. Given the result above      .1000111100000000
        //      Chop off bits until we get  00000000.10001111
        this.y.mantissa = (short) (Utils.short_unsigned_right_shift(temp, 8));
    }

    private int getLeftMostSetBitDistance(int fixed)
    {
        int bitDistance = 0;

        while (fixed > 1){
            fixed /= 2;
            bitDistance++;
        }

        return (bitDistance);
    }
}
