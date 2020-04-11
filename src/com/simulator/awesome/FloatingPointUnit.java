package com.simulator.awesome;

/* General overview:
    1. Rewrite the smaller number so that its exponent matches with the exponent of the larger number.
    2. If either mantissa is negative, convert it to 2's complement.
    3. For addition, add the mantissas. For subtraction, subtract the mantissas.
    4. If the result mantissa is negative, convert it back to signed magnitude and set the sign bit appropriately.
    5. Try to handle overflow. If we can't, set overflow.
    6. Set the result to y.
*/

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

    public short getYAsShort() {
        return this.y.toShort();
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

    public short convertFloatingPointToFixedPoint(){
        int signedExponent = this.a.exponentValue;
        if (this.a.exponentSign == 1){
            signedExponent = -this.a.exponentValue;
        }
        return (short) round((this.a.mantissa * (2^signedExponent)));
    }

    public void convertFixedPointToFloatingPoint(){
        // todo
    }
}
