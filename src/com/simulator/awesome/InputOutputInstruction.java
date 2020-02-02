package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by IN, OUT, CHK
public class InputOutputInstruction {
    public short opCode;
    public short registerId;
    public short deviceId;

    InputOutputInstruction(short word) {
        short opCodeMask             = (short) 0b1111110000000000;
        short registerMask           = (short) 0b0000001100000000;
        short deviceMask             = (short) 0b0000000000111111;

        short deviceOffset           = 0;
        short registerOffset         = 8;
        short opCodeOffset           = 10;

        this.deviceId = (short)((word & deviceMask) >>> deviceOffset);
        this.registerId = (short)((word & registerMask) >>> registerOffset);
        // I am unclear why I need to do this additional mask. If I don't do it, shifting right pads with 1 bits
        this.opCode = (short) (((word & opCodeMask) >>> opCodeOffset) & 0b0000000000111111);
    }

    public void print(){
        System.out.println("OpCode: " + Short.toUnsignedInt(this.opCode));
        System.out.println("Register ID: " + this.registerId);
        System.out.println("Device ID: " + this.deviceId);
    }
}
