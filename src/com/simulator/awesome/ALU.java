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
        this.context.setOverflow(Integer.compareUnsigned(result, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(result, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);

        // Write result to proper output register
        this.y = result;
    }

    public void subtract(){
        // Calculate Result
        int result = this.a - this.b;

        // Set Condition Variables
        this.context.setOverflow(Integer.compareUnsigned(result, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(result, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);

        // Write result to proper output register
        this.y = result;
    }

    public void compare(){
        // Set Condition Variables
        this.context.setOverflow(false);
        this.context.setUnderflow(false);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);

        // Just clear Y
        this.y = 0;
    }

    // Decrements A and then sets condition variables comparing with B
    public void decrementAndCompare() {
        int result = this.a - 1;

        // Set Condition Variables
        this.context.setOverflow(Integer.compareUnsigned(result, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(result, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(result, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(result, this.b) > 0);

        this.y = result;
    }

    public void multiply() {
        int result = this.a * this.b;

        this.context.setOverflow(Integer.compareUnsigned(result, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(result, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);

        this.y = result;
    }

    public void divide() {
        this.y = Integer.divideUnsigned(this.a, this.b);
        this.y2 = Integer.remainderUnsigned(this.a, this.b);

        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0 || Integer.compareUnsigned(this.y2, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0 || Integer.compareUnsigned(this.y2, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(Integer.compareUnsigned(this.b, 0) == 0);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);
    }

    public void and(){
        this.y = this.a & this.b;
        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);
    }

    public void or(){
        this.y = this.a | this.b;
        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);
    }

    public void not(){
        this.y = ~this.a;
        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(false);
        this.context.setGreaterThan(false);
    }

    // TODO: This overflow / underflow logic might need to check if we shift meaningful bits off?
    public void arithmeticShiftLeft(){
        this.y = this.a << this.b;

        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);
    }

    public void arithmeticShiftRight(){
        this.y = this.a >> this.b;

        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);
    }
    public void logicalShiftLeft(){
        // Note: Logical and arithmetic left shifts operate identically.
        // https://www.quora.com/Why-is-there-no-unsigned-left-shift-operator-in-Java
        this.y = this.a << this.b;

        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);
    }

    public void logicalShiftRight(){
        this.y = Utils.short_unsigned_right_shift(this.a, this.b );

        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);
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

        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);
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

        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);
    }
    public void logicalRotateLeft(){
        this.y = ((this.a << this.b) | Utils.short_unsigned_right_shift(this.a, (Short.SIZE - this.b)));

        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);
    }
    public void logicalRotateRight(){
        this.y = Utils.short_unsigned_right_shift(this.a, this.b) | (this.a << (Short.SIZE - this.b));

        this.context.setOverflow(Integer.compareUnsigned(this.y, Config.MAX_VALUE) > 0);
        this.context.setUnderflow(Integer.compareUnsigned(this.y, Config.MIN_VALUE) < 0);
        this.context.setDivideByZero(false);
        this.context.setEqual(Integer.compareUnsigned(this.a, this.b) == 0);
        this.context.setGreaterThan(Integer.compareUnsigned(this.a, this.b) > 0);
    }

    public short getYAsShort() {
        return (short) (this.y << 16 >>> 16);
    }

    public short getY2AsShort() {
        return (short) (this.y2 << 16 >>> 16);
    }

    public short[] getYAsShorts() {
        short[] results = new short[2];
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

    public int getAAsInt() { return this.a; }

}
