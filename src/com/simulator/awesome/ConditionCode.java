package com.simulator.awesome;

// Condition code.
// Uses the least significant 4 bits
// set when arithmetic/logical operations are executed
// it has four 1-bit elements: overflow, underflow, division by zero, equal-or-not.
// ID   |   Fault
// 0001 |   Overflow
// 0010 |   Underflow
// 0100 |   Division by Zero
// 1000 |   Equal

import static com.simulator.awesome.Utils.*;

// Note: Adding Upper bits that cannot be addressed by OPCODE 12 - Jump If Condition Code
// 10000 |   Greater Than
public class ConditionCode {
    private byte cc;

    ConditionCode(){
        this.cc = 0;
    }

    // Condition Code (CC) getters and setters

    public byte get() { return (byte) (this.cc); };

    public boolean isOverflow() {
        return getNthLeastSignificantBit(this.cc, 0);
    }

    public void setOverflow(boolean isOverflow) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 0, isOverflow);
    }

    public boolean isUnderflow() {
        return getNthLeastSignificantBit(this.cc, 1);
    }

    public void setUnderflow(boolean isUnderflow) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 1, isUnderflow);
    }

    public boolean isDivideByZero() {
        return getNthLeastSignificantBit(this.cc, 2);
    }

    public void setDivideByZero(boolean isDivideByZero) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 2, isDivideByZero);
    }

    public boolean isEqual() {
        return getNthLeastSignificantBit(this.cc, 3);
    }

    public void setEqual(boolean isEqualOrNot) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 3, isEqualOrNot);
    }

    public boolean isGreaterThan() {
        return getNthLeastSignificantBit(this.cc, 4);
    }

    public void setGreaterThan(boolean isGreaterThan) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 4, isGreaterThan);
    }

    // There isn't a bit for this directly, but we can determine this if not greater than or equal
    public boolean isLessThan() {
        return !this.isEqual() && !this.isGreaterThan();
    }

    public boolean isCondition(int conditionCode){
        switch (conditionCode) {
            case 0:
                return this.isOverflow();
            case 1:
                return this.isUnderflow();
            case 2:
                return this.isDivideByZero();
            case 3:
                return this.isEqual();
            default:
                return false;
        }
    }

    public String toString(){
        return wordToString(this.get()).substring(12,16);
    }
}
