package com.simulator.awesome;

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

    // Resolves the appropriate index register and address offset, setting the result to IAR
    // IAR <- IR(address field)
    // Move the first operand address from the Instruction Register to the Internal Address Register
    // IAR<- IAR + X(index field)
    // If the operand is indexed, add the contents of the specified index register to the IAR
    public void computeEffectiveAddress() {
        this.context.setInternalAddressRegister((short) (this.context.getIndexRegister(this.indexRegisterId) + this.address));
    }

    // Given the address of a pointer stored in the IAR
    // Moves the contests of the IAR to the MAR
    // Retrieves the address the pointer is storing to the MBR
    // Writes the address in the MBR to the IAR
    public void evaluatePointerToAddress() {
        // Move the contents of the IAR to the MAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // Fetch the contents of the word in memory specified by the MAR into the MBR.
        this.context.fetchMemoryAddressRegister();
        // Copy the MBR to the IAR
        this.context.setInternalAddressRegister(this.context.getMemoryBufferRegister());
    }

    // Resolves the operand address, resolving the pointer (indirect) to the address it contains if needed
    // The end state is that the operand is loaded as an address to the IAR
    public void fetchOperand(){
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // MAR <- IR
        // Move the contents of the IAR to the MAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // Fetch the contents of the word in memory specified by the MAR into the MBR.
//        this.context.fetchMemoryAddressRegister();
    }

    public void print(){
        System.out.println("OpCode: " + this.opCode);
        System.out.println("Register ID: " + this.registerId);
        System.out.println("Index Register ID: " + this.indexRegisterId);
        System.out.println("Is Indirect? : " + this.isIndirect);
        System.out.println("Address: " + this.address);
    }
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
class LoadRegisterFromMemory extends LoadStoreInstruction {
    public LoadRegisterFromMemory(short word, Simulator context){
        super(word, context);
    }

    // Evaluates the address and copies the data at that address to the MBR
    // If indirect is set, dereferences the pointer and writes the resulting value to the MBR
    public void fetchOperand(){
        super.fetchOperand();
        // MAR <- IAR - Move the contents of the IAR to the MAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // Fetch the contents of the word in memory specified by the MAR into the MBR.
        this.context.fetchMemoryAddressRegister();
    }

    public void execute(){
        // NOOP: We're just storing the operand in a register, so nothing to execute
    }

    public void storeResult(){
        this.context.setGeneralRegister(this.registerId, this.context.getMemoryBufferRegister());
    }
}

/**
 * OPCODE 02 - Store Register To Memory
 * Octal: 002
 * STR r, x, address[,I]
 * r = 0..3
 * Memory(EA) <- c(r)
 */
class StoreRegisterToMemory extends LoadStoreInstruction {
    public StoreRegisterToMemory(short word, Simulator context) {
        super(word, context);
    }

    // Uses the default fetchOperand hook, which resolve the Memory Address that we want to write to
    // (including chasing pointers as needed) and then store it in the MAR

    // Copies the register value to the MBR
    public void execute() {
        this.context.setMemoryBufferRegister(this.context.getGeneralRegister(this.registerId));
    }

    // Copy the value of MBR to the address in memory stored in the MAR
    public void storeResult(){
        this.context.setWord(this.context.getMemoryAddressRegister(), this.context.getMemoryBufferRegister());
    }
}

/**
 OPCODE 03 - Load Register with Address
 Octal: 003
 LDA r, x, address[,I]
 r = 0..3
 r <- EA
 */
class LoadRegisterWithAddress extends LoadStoreInstruction {
    public LoadRegisterWithAddress(short word, Simulator context) {
        super(word, context);
    }

    // Uses the default fetchOperand hook, which resolve the Memory Address that we want to load
    // (including chasing pointers as needed) and then store it in the MAR

    public void execute(){
        // NOOP
    }

    // Write the address in the MAR to the register
    public void storeResult(){
        this.context.setGeneralRegister(this.registerId, this.context.getMemoryAddressRegister());
    }
}

/**
 OPCODE 41 - Load Index Register from Memory
 Octal: 051
 LDX x, address[,I]
 x = 1..3
 Xx <- c(EA)
 */
class LoadIndexRegisterFromMemory extends LoadStoreInstruction {
    public LoadIndexRegisterFromMemory(short word, Simulator context) {
        super(word, context);
    }

    // Evaluates the address and copies the data at that address to the MBR
    // If indirect is set, dereferences the pointer and writes the resulting value to the MBE
    // Note that we use the Index register as the source or destination, so it is not used for indexing
    // This means that this operation CAN ONLY LOAD from words 0-32
    // To use higher addresses, we have to use indirect addressing
    public void fetchOperand(){
        // No indexing possible, so just set IAR to address
        this.context.setInternalAddressRegister(this.address);
        if (this.isIndirect) this.evaluatePointerToAddress();

        // MAR <- IAR - Move the contents of the IAR to the MAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // Fetch the contents of the word in memory specified by the MAR into the MBR.
        this.context.fetchMemoryAddressRegister();
    }

    public void execute() {
        // NOOP
    }

    public void storeResult(){
        this.context.setIndexRegister(this.indexRegisterId, this.context.getMemoryBufferRegister());
    }
}

/**
 OPCODE 42 - Store Index Register to Memory
 Octal: 052
 STX x, address[,I]
 X = 1..3
 Memory(EA) <- c(Xx)
 */
class StoreIndexRegisterToMemory extends LoadStoreInstruction {
    public StoreIndexRegisterToMemory(short word, Simulator context) {
        super(word, context);
    }

    // Uses the default fetchOperand hook, which resolve the Memory Address that we want to write to
    // (including chasing pointers as needed) and then store it in the MAR
    public void fetchOperand(){
        // No indexing possible, so just set IAR to address
        this.context.setInternalAddressRegister(this.address);
        if (this.isIndirect) this.evaluatePointerToAddress();

        // MAR <- IAR - Move the contents of the IAR to the MAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
    }

    // Copies the index register value to the MBR
    public void execute() {
        this.context.setMemoryBufferRegister(this.context.getIndexRegister(this.indexRegisterId));
    }

    // Copy the value of MBR to the address in memory stored in the MAR
    public void storeResult(){
        this.context.setWord(this.context.getMemoryAddressRegister(), this.context.getMemoryBufferRegister());
    }
}