package com.simulator.awesome;

public class ALU {
    // a and be are input registers for the ALU
    private short a;
    private short b;

    // y and y2 are the output registers for the ALU
    // y2 is used by divide
    private int y;
    private int y2;

    private Simulator context;

    ALU(Simulator context) {
        this.a = 0;
        this.b = 0;
        this.y = 0;
        this.context = context;
    }

    public void setA(short a) {
        this.a = a;
    }

    public void setB(short b) {
        this.b = b;
    }

    public void add(){
        // Calculate Result
        int result = this.a + this.b;

        // Set Condition Variables
        this.context.setOverflow(result > Short.MAX_VALUE);
        this.context.setUnderflow(result < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);

        // Write result to proper output register
        this.y = result;
    }

    public void subtract(){
        // Calculate Result
        int result = this.a - this.b;

        // Set Condition Variables
        this.context.setOverflow(result > Short.MAX_VALUE);
        this.context.setUnderflow(result < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);

        // Write result to proper output register
        this.y = result;
    }

    public void compare(){
        // Set Condition Variables
        this.context.setOverflow(false);
        this.context.setUnderflow(false);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);

        // Just clear Y
        this.y = 0;
    }

    // Decrements A and then sets condition variables comparing with B
    public void decrementAndCompare() {
        int result = this.a - 1;

        // Set Condition Variables
        this.context.setOverflow(result > Short.MAX_VALUE);
        this.context.setUnderflow(result < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(result == this.b);
        this.context.setGreaterThan(result > this.b);

        this.y = result;
    }

    public void multiply() {
        int result = this.a * this.b;

        this.context.setOverflow(result > Integer.MAX_VALUE);
        this.context.setUnderflow(result < Integer.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);

        this.y = result;
    }

    public void divide() {
        this.y = this.a / this.b;
        this.y2 = this.a % this.b;

        this.context.setOverflow(this.y > Short.MAX_VALUE || this.y2 > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE || this.y2 < Short.MIN_VALUE);
        this.context.setDivideByZero(this.b == 0);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);
    }

    public void and(){
        this.y = this.a & this.b;
        this.context.setOverflow(this.y > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);
    }

    public void or(){
        this.y = this.a | this.b;
        this.context.setOverflow(this.y > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);
    }

    public void not(){
        this.y = ~this.a;
        this.context.setOverflow(this.y > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(false);
        this.context.setGreaterThan(false);
    }

    // TODO: This overflow / underflow logic might need to check if we shift meaningful bits off?
    public void arithmeticShiftLeft(){
        this.y = this.a << this.b;

        this.context.setOverflow(this.y > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);
    }

    public void arithmeticShiftRight(){
        this.y = this.a >> this.b;

        this.context.setOverflow(this.y > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);
    }
    public void logicalShiftLeft(){
        // Note: Logical and arithmetic left shifts operate identically.
        // https://www.quora.com/Why-is-there-no-unsigned-left-shift-operator-in-Java
        this.y = this.a << this.b;

        this.context.setOverflow(this.y > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);
    }

    public void logicalShiftRight(){
        this.y = Utils.short_unsigned_right_shift(this.a, this.b );

        this.context.setOverflow(this.y > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);
    }
    public void arithmeticRotateLeft(){
        // Arithmetic rotation - preserve the sign bit, rotating all other bits
        short SIGN_BIT_MASK = (short)0b1000000000000000;
        short LEAST_SIGNIFICANT_BITS_MASK = (short)0b0111111111111111;
        // Save the sign bit
        short signBit = (short) (SIGN_BIT_MASK & this.a);

        // Discard the left-most bit by shifting left by 1 and right by 1.
        short leastSignificantBits = this.a;
        leastSignificantBits <<= 1;
        leastSignificantBits >>>= 1;

        this.y = (signBit | (LEAST_SIGNIFICANT_BITS_MASK & (leastSignificantBits << this.b) | Utils.short_unsigned_right_shift(leastSignificantBits, (Short.SIZE - this.b - 1))));

//        this.context.setZ(signBit | (LEAST_SIGNIFICANT_BITS_MASK & (Utils.short_unsigned_right_shift(leastSignificantBits, this.count) | (leastSignificantBits << (Short.SIZE - this.count - 1)))));

        this.context.setOverflow(this.y > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);
    }
    public void arithmeticRotateRight(){
        // Arithmetic rotation - preserve the sign bit, rotating all other bits
        short SIGN_BIT_MASK = (short)0b1000000000000000;
        short LEAST_SIGNIFICANT_BITS_MASK = (short)0b0111111111111111;
        // Save the sign bit
        short signBit = (short) (SIGN_BIT_MASK & this.a);

        // Discard the left-most bit by shifting left by 1 and right by 1.
        short leastSignificantBits = this.a;
        leastSignificantBits <<= 1;
        leastSignificantBits >>>= 1;

        this.y = (signBit | (LEAST_SIGNIFICANT_BITS_MASK & (Utils.short_unsigned_right_shift(leastSignificantBits, this.b) | (leastSignificantBits << (Short.SIZE - this.b - 1)))));

        this.context.setOverflow(this.y > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);
    }
    public void logicalRotateLeft(){
        this.y = ((this.a << this.b) | Utils.short_unsigned_right_shift(this.a, (Short.SIZE - this.b)));

        this.context.setOverflow(this.y > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);
    }
    public void logicalRotateRight(){
        this.y = Utils.short_unsigned_right_shift(this.a, this.b) | (this.a << (Short.SIZE - this.b));

        this.context.setOverflow(this.y > Short.MAX_VALUE);
        this.context.setUnderflow(this.y < Short.MIN_VALUE);
        this.context.setDivideByZero(false);
        this.context.setEqual(this.a == this.b);
        this.context.setGreaterThan(this.a > this.b);
    }

    public short getYAsShort() {
        return (short) (this.y << 16 >>> 16);
    }

    public short getY2AsShort() {
        return (short) (this.y2 << 16 >>> 16);
    }

    public short[] getYAsShorts() {
        short[] results = {};
        results[0] = (short)(this.y >>> 16);
        results[1] = (short)(this.y << 16 >>> 16);
        return results;
    }

    public int getYAsInt() {
        return this.y;
    }

    public boolean getYAsBool() {
        return this.y == 1;
    }


}