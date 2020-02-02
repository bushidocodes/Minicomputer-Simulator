package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by LDR, STR, LDA, LDX, STX, JZ, JNE, JCC, JMA, JSR, RFS, SOB, JGE, AMR, SMR, AIR, SIR
public class LoadStoreInstruction {
    public short address;
    public boolean isIndirect;
    public short indexRegisterId;
    public short registerId;
    public short opCode;

    LoadStoreInstruction(short word) {
        short opCodeMask             = (short) 0b1111110000000000;
        short registerMask           = (short) 0b0000001100000000;
        short indexRegisterMask      = (short) 0b0000000011000000;
        short indirectAddressingMask = (short) 0b0000000000100000;
        short addressMask            = (short) 0b0000000000011111;

        short addressOffset            = 0;
        short indexRegisterOffset      = 6;
        short registerOffset           = 8;
        short opCodeOffset             = 10; // Why is this 11 above? I am off by one somewhere

        this.address = (short)((word & addressMask) >>> addressOffset);
        this.isIndirect = (word & indirectAddressingMask) == indirectAddressingMask;
        this.indexRegisterId = (short)((word & indexRegisterMask) >>> indexRegisterOffset);
        this.registerId = (short)((word & registerMask) >>> registerOffset);
        this.opCode = (short) (((word & opCodeMask) >>> opCodeOffset) & 0b0000000000111111);
    }

    public void print(){
        System.out.println("OpCode: " + this.opCode);
        System.out.println("Register ID: " + this.registerId);
        System.out.println("Index Register ID: " + this.indexRegisterId);
        System.out.println("Is Indirect? : " + this.isIndirect);
        System.out.println("Address: " + this.address);
    }
}
