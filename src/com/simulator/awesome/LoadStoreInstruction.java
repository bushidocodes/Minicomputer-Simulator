package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by LDR, STR, LDA, LDX, STX, JZ, JNE, JCC, JMA, JSR, RFS, SOB, JGE, AMR, SMR, AIR, SIR
public class LoadStoreInstruction extends Instruction {
    public short registerId;
    public short indexRegisterId;   // Acts as base address
    public boolean isIndirect;
    public short address;           // Acts as offset

    LoadStoreInstruction(short word, Simulator context) {
        super(word, context);
        short registerMask           = (short) 0b0000001100000000;
        short indexRegisterMask      = (short) 0b0000000011000000;
        short indirectAddressingMask = (short) 0b0000000000100000;
        short addressMask            = (short) 0b0000000000011111;

        short addressOffset            = 0;
        short indexRegisterOffset      = 6;
        short registerOffset           = 8;

        this.address = (short)((word & addressMask) >>> addressOffset);
        this.isIndirect = (word & indirectAddressingMask) == indirectAddressingMask;
        this.indexRegisterId = (short)((word & indexRegisterMask) >>> indexRegisterOffset);
        this.registerId = (short)((word & registerMask) >>> registerOffset);
    }

    public void fetchOperand(){
        // IAR <- IR(address field)
        // Move the first operand address from the Instruction Register to the Internal Address Register
        // IAR<- IAR + X(index field)
        // If the operand is indexed, add the contents of the specified index register to the IAR
        if(this.indexRegisterId == 0){
            // No indexing. Move the address into IAR.
            this.context.iar = this.address;
        } else if (this.indexRegisterId == 1){
            this.context.iar = (short) (this.context.x1 + this.address);
        } else if (this.indexRegisterId == 2){
            this.context.iar = (short) (this.context.x2 + this.address);
        } else if (this.indexRegisterId == 3){
            this.context.iar = (short) (this.context.x3 + this.address);
        }
        // MAR <- IR
        // Move the contents of the IAR to the MAR
        this.context.setMemoryAddressRegister(this.context.iar);
        // Fetch the contents of the word in memory specified by the MAR into the MBR.
        this.context.fetchMemoryAddressRegister();
        // If this is an indirect, we need to do this again
        if (this.isIndirect) {
            this.context.setMemoryAddressRegister(this.context.getMemoryBufferRegister());
            this.context.fetchMemoryAddressRegister();
        }
    }

    public void execute() {
        // NOOP
    }

    public void print(){
        System.out.println("OpCode: " + this.opCode);
        System.out.println("Register ID: " + this.registerId);
        System.out.println("Index Register ID: " + this.indexRegisterId);
        System.out.println("Is Indirect? : " + this.isIndirect);
        System.out.println("Address: " + this.address);
    }
}

class LoadRegisterFromMemory extends LoadStoreInstruction {
    public LoadRegisterFromMemory(short word, Simulator context){
        super(word, context);
    }

    /**
     OPCODE 01 - Load Register From Memory
     Octal: 001
     LDR r, x, address[,I]
     r = 0..3
     r <- c(EA)
     note that EA is computed as given above

     In one cycle, move the data from the MBR to an Internal Result Register (IRR)
     */
    public void execute(){
        System.out.println("LDR");
    }

    public void storeResult(){
        // NOOP
    }
}


class StoreRegisterToMemory extends LoadStoreInstruction {
    public StoreRegisterToMemory(short word, Simulator context) {
        super(word, context);
    }

    /**
     * OPCODE 02 - Store Register To Memory
     * Octal: 002
     * STR r, x, address[,I]
     * r = 0..3
     * Memory(EA) <- c(r)
     */
    public void execute() {
        System.out.println("STR");
    }

    public void storeResult(){
        // NOOP
    }
}

class LoadRegisterWithAddress extends LoadStoreInstruction {
    public LoadRegisterWithAddress(short word, Simulator context) {
        super(word, context);
    }
    /**
     OPCODE 03 - Load Register with Address
     Octal: 003
     LDA r, x, address[,I]
     r = 0..3
     r <- EA
     */
    public void execute(){
        System.out.println("LDA");
    }

    public void storeResult(){
        // NOOP
    }
}

