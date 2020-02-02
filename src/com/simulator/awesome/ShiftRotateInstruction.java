package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by LDR, STR, LDA, LDX, STX

enum ShiftRotateType {
    LOGICAL, ARITHMETIC
}

enum ShiftRotateDirection {
    LEFT, RIGHT
}

public class ShiftRotateInstruction {
    public short opCode;
    public short registerId;
    public ShiftRotateType type; // If not logical, is arithmetic
    public ShiftRotateDirection direction;
    public short count;

    ShiftRotateInstruction(short word) {
        short opCodeMask             = (short) 0b1111110000000000;
        short registerMask           = (short) 0b0000001100000000;
        short logicalArithmeticMask  = (short) 0b0000000010000000;
        short leftRightMask          = (short) 0b0000000001000000;
        short countMask              = (short) 0b0000000000001111;

        short countOffset            = 0;
        short registerOffset         = 8;
        short opCodeOffset           = 10;

        this.count = (short)((word & countMask) >>> countOffset);
        this.type = (word & logicalArithmeticMask) == logicalArithmeticMask ? ShiftRotateType.LOGICAL : ShiftRotateType.ARITHMETIC;
        this.direction = (word & leftRightMask) == leftRightMask ? ShiftRotateDirection.LEFT : ShiftRotateDirection.RIGHT;
        this.registerId = (short)((word & registerMask) >>> registerOffset);
        this.opCode = (short) (((word & opCodeMask) >>> opCodeOffset) & 0b0000000000111111);
    }

    public void print(){
        System.out.println("OpCode: " + Short.toUnsignedInt(this.opCode));
        System.out.println("Register ID: " + this.registerId);
        System.out.println("Type: " + this.type);
        System.out.println("Direction : " + this.direction);
        System.out.println("Count: " + this.count);
    }
}
