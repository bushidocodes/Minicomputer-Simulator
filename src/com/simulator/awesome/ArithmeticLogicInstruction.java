package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by MLT, DVD, TRR, AND, ORR, NOT
public class ArithmeticLogicInstruction {
    public short opCode;
    public short firstRegisterId;
    public short secondRegisterId;

    ArithmeticLogicInstruction(short word) {
        short opCodeMask                = (short) 0b1111110000000000;
        short firstRegisterMask         = (short) 0b0000001100000000;
        short secondRegisterMask        = (short) 0b0000000011000000;

        short secondRegisterOffset      = 6;
        short firstRegisterOffset       = 8;
        short opCodeOffset              = 10;

        this.secondRegisterId = (short)((word & secondRegisterMask) >>> secondRegisterOffset);
        this.firstRegisterId = (short)((word & firstRegisterMask) >>> firstRegisterOffset);
        this.opCode = (short) (((word & opCodeMask) >>> opCodeOffset) & 0b0000000000111111);
    }

    public void print(){
        System.out.println("OpCode: " + this.opCode);
        System.out.println("First Register ID: " + this.firstRegisterId);
        System.out.println("Second Register ID: " + this.secondRegisterId);
    }
}
