package com.simulator.awesome;

public class RegisterMemoryInstruction extends Instruction {
    final public short registerId;
    final public short indexRegisterId;   // Acts as base address
    final public boolean isIndirect;
    final public short address;           // Acts as offset

    RegisterMemoryInstruction(short word, Simulator context) {
        super(word, context);
        short registerMask           = (short) 0b0000001100000000;
        short indexRegisterMask      = (short) 0b0000000011000000;
        short indirectAddressingMask = (short) 0b0000000000100000;
        short addressMask            = (short) 0b0000000000011111;

        short addressOffset            = 0;
        short indexRegisterOffset      = 6;
        short registerOffset           = 8;

        this.address = Utils.short_unsigned_right_shift((short)(word & addressMask), addressOffset);
        this.isIndirect = (word & indirectAddressingMask) == indirectAddressingMask;
        this.indexRegisterId = Utils.short_unsigned_right_shift((short)(word & indexRegisterMask), indexRegisterOffset);
        this.registerId = Utils.short_unsigned_right_shift((short)(word & registerMask), registerOffset);

        this.validateGeneralRegisterIndex(this.registerId);
        this.validateIndexRegisterIndex(this.indexRegisterId);
    }

    public void computeEffectiveAddress() {
        // IAR <- EA
        this.context.setInternalAddressRegister((short) (this.context.getIndexRegister(this.indexRegisterId) + this.address));
    }

    public void evaluatePointerToAddress() {
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(MAR)
        this.context.fetchMemoryAddressRegister();
        // IAR <- MBR
        this.context.setInternalAddressRegister(this.context.getMemoryBufferRegister());
    }

    // Resolves the operand address, resolving the pointer (indirect) to the address it contains if needed
    // The end state is that the operand is loaded as an address to the IAR
    public void fetchOperand(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
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
class LoadRegisterFromMemory extends RegisterMemoryInstruction {
    public LoadRegisterFromMemory(short word, Simulator context){
        super(word, context);
        this.validateGeneralRegisterIndex(this.registerId);
        this.validateIndexRegisterIndex(this.indexRegisterId);
    }

    public void fetchOperand(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(MAR)
        this.context.fetchMemoryAddressRegister();
        // RX <- MBR
        this.context.setGeneralRegister(this.registerId, this.context.getMemoryBufferRegister());
    }

    public void execute(){
        // NOOP
    }

    public void storeResult(){
        // NOOP
    }
}

/**
 * OPCODE 02 - Store Register To Memory
 * Octal: 002
 * STR r, x, address[,I]
 * r = 0..3
 * Memory(EA) <- c(r)
 */
class StoreRegisterToMemory extends RegisterMemoryInstruction {
    public StoreRegisterToMemory(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand() {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- RX
        this.context.setMemoryBufferRegister(this.context.getGeneralRegister(this.registerId));
        // c(MAR) <- MBR
        this.context.setWord(this.context.getMemoryAddressRegister(), this.context.getMemoryBufferRegister());
    }

    public void execute() {
        // NOOP
    }

    public void storeResult(){
        // NOOP
    }
}

/**
 OPCODE 03 - Load Register with Address
 Octal: 003
 LDA r, x, address[,I]
 r = 0..3
 r <- EA
 */
class LoadRegisterWithAddress extends RegisterMemoryInstruction {
    public LoadRegisterWithAddress(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand() {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        // Note: If this is an indirect, this ends up being a memory seek with more than one cycle
        // Disregarding this for simplicity
        if (this.isIndirect) this.evaluatePointerToAddress();
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // RX <- MAR
        this.context.setGeneralRegister(this.registerId, this.context.getMemoryAddressRegister());
    }

    public void execute(){
        // NOOP
    }

    public void storeResult(){
        // NOOP
    }
}

/**
 OPCODE 41 - Load Index Register from Memory
 Octal: 051
 LDX x0, x1, address[,I]
 x0 is the  destination index register to be loaded from memory, where x0 = 1..3
 x1 is the index register to be used for calculating the effective address, where x1 = 1..3
 Xx <- c(EA)
 */
class LoadIndexRegisterFromMemory extends RegisterMemoryInstruction {
    final private short destinationIndexRegisterId;
    public LoadIndexRegisterFromMemory(short word, Simulator context) {
        super(word, context);
        this.destinationIndexRegisterId = this.registerId;
    }

    public void fetchOperand() {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(MAR)
        this.context.fetchMemoryAddressRegister();
        // X0 <- MBR
        this.context.setIndexRegister(this.destinationIndexRegisterId, this.context.getMemoryBufferRegister());
    }

    public void execute() {
        // NOOP
    }

    public void storeResult(){
        // NOOP
    }
}

/**
 OPCODE 42 - Store Index Register to Memory
 Octal: 052
 STX x0, x1, address[,I]
 x0 is the source register to be loaded, where x0 = 1..3
 x1 is the indexing register to be used for calculating the effective address, where x1 = 1..3
 Memory(EA) <- c(Xx)
 */
class StoreIndexRegisterToMemory extends RegisterMemoryInstruction {
    final private short sourceIndexRegisterId;

    public StoreIndexRegisterToMemory(short word, Simulator context) {
        super(word, context);
        this.sourceIndexRegisterId = this.registerId;
    }

    public void fetchOperand() {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- X0
        this.context.setMemoryBufferRegister(this.context.getIndexRegister(this.sourceIndexRegisterId));
        // c(MAR) <- MBR
        this.context.setWord(this.context.getMemoryAddressRegister(), this.context.getMemoryBufferRegister());
    }

    public void execute() {
        // NOOP
    }

    public void storeResult(){
        // NOOP
    }
}


/**
 OPCODE 10 - Jump If Zero
 Octal: 012
 JZ r, x, address[,I]
 If c(r) = 0, then PC <- EA
 Else PC <- PC+1
 */
class JumpIfZero extends RegisterMemoryInstruction {
    public JumpIfZero(short word, Simulator context) {
        super(word, context);
    }

    // Sets the IAR with the address we conditionally might want to set the PC to
    public void fetchOperand(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // a <- RX
        this.context.alu.setA(this.context.getGeneralRegister(this.registerId));
        // b <- 0
        this.context.alu.setB((short) 0);
    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.compare();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.context.isEqual()) {
            // IAR <- EA
            computeEffectiveAddress();
            if (this.isIndirect) this.evaluatePointerToAddress();
            // PC <- IAR
            this.context.setProgramCounter(this.context.getInternalAddressRegister());
            this.context.setDidBranch();
        }
    }
}

/**
 OPCODE 11 - Jump If Not Equal
 Octal: 013
 JNE r, x, address[,I]
 If c(r) != 0, then PC <-- EA
 Else PC <- PC + 1
 */
class JumpIfNotEqual extends RegisterMemoryInstruction {
    public JumpIfNotEqual(short word, Simulator context) {
        super(word, context);
    }

    // Sets the IAR with the address we conditionally might want to set the PC to
    public void fetchOperand(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // a <- RX
        this.context.alu.setA(this.context.getGeneralRegister(this.registerId));
        // b <- 0
        this.context.alu.setB((short) 0);

    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.compare();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.context.isEqual() == false) {
            // PC <- IAR
            this.context.setProgramCounter(this.context.getInternalAddressRegister());
            this.context.setDidBranch();
        }
        // NOOP
    }
}

/**
 * OPCODE 12 - Jump If Condition Code
 * Octal: 014
 * JCC cc, x, address[,I]
 * cc replaces r for this instruction
 * cc takes values 0, 1, 2, 3 as above and specifies the bit in the Condition Code Register to check;
 * If cc bit  = 1, PC <- EA
 * Else PC <- PC + 1
 **/
class JumpIfConditionCode extends RegisterMemoryInstruction {
    private int conditionCode;

    public JumpIfConditionCode(short word, Simulator context) {
        super(word, context);
        this.conditionCode = this.registerId;
    }

    public void fetchOperand(){
        if (this.context.isCondition(this.conditionCode)){
            this.computeEffectiveAddress();
            if (this.isIndirect) this.evaluatePointerToAddress();
            this.context.setProgramCounter(this.context.getInternalAddressRegister());
            this.context.setDidBranch();
        }
    }

    public void execute() {
        // NOOP
    }

    public void storeResult(){
        // NOOP
    }
}

/**
 * OPCODE 13 - Unconditional Jump To Address
 * Octal: 015
 * JMA x, address[,I]
 * PC <- EA,
 * Note: r is ignored in this instruction
 */
class UnconditionalJumpToAddress extends RegisterMemoryInstruction {
    public UnconditionalJumpToAddress(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        this.computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // PC <- IAR
        this.context.setProgramCounter(this.context.getInternalAddressRegister());
        this.context.setDidBranch();
    }

    public void execute() {
        // NOOP
    }

    public void storeResult(){
        // NOOP
    }
}


//TODO: I have not idea about the comment below about the argument list. Where do we store the arguments we're passing to the subroutine?

/**
 OPCODE 14 - Jump and Save Return Address
 Octal: 016
 JSR x, address[,I]
 R3 <- PC+1;
 PC <- EA
 R0 should contain pointer to arguments
 Argument list should end with –1 (all 1s) value
 */
class JumpAndSaveReturnAddress extends RegisterMemoryInstruction {
    public JumpAndSaveReturnAddress(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        // IAR <- EA
        this.computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // R3 <- PC+1
        this.context.setGeneralRegister((short) 3, (short) (this.context.getProgramCounter() + 1));
        // PC <- IAR
        this.context.setProgramCounter(this.context.getInternalAddressRegister());
        this.context.setDidBranch();
    }

    public void execute() {
        // NOOP
    }

    public void storeResult(){
        // NOOP
    }
}

/**
 OPCODE 15 - Return From Subroutine
 Octal: 017
 w/ return code as Immed portion (optional) stored in the instruction’s address field.
 RFS Immed
 R0 <- Immed; PC <- c(R3)
 IX, I fields are ignored.
 */
class ReturnFromSubroutine extends RegisterMemoryInstruction {
    short immediateValue;

    public ReturnFromSubroutine(short word, Simulator context) {
        super(word, context);
        this.immediateValue = this.address;
    }

    public void fetchOperand(){
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.setGeneralRegister((short) 0, this.immediateValue);
        this.context.setProgramCounter(this.context.getGeneralRegister((short) 3));
        this.context.setDidBranch();
    }

    public void execute() {
        // NOOP
    }

    public void storeResult() {
        // NOOP
    }
}

/**
 OPCODE 16 - Subtract One and Branch.
 Octal: 020
 SOB r, x, address[,I]
 r = 0..3
 r <- c(r) – 1
 If c(r) > 0,  PC <- EA;
 Else PC <- PC + 1
 */
class SubtractOneAndBranch extends RegisterMemoryInstruction {
    public SubtractOneAndBranch(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        // IAR <- EA
        this.computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        this.context.alu.setA(this.context.getGeneralRegister(this.registerId));
        this.context.alu.setB((short)0);

    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.decrementAndCompare();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.context.isGreaterThan()){
            // PC <- IAR
            this.context.setProgramCounter(this.context.getInternalAddressRegister());
            this.context.setDidBranch();
        }
    }
}


/**
 OPCODE 17 - Jump Greater Than or Equal To
 Octal: 021
 JGE r,x, address[,I]
 If c(r) >= 0, then PC <- EA
 Else PC <- PC + 1
 */
class JumpGreaterThanOrEqualTo extends RegisterMemoryInstruction {
    public JumpGreaterThanOrEqualTo(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        // IAR <- EA
        this.computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        this.context.alu.setA(this.context.getGeneralRegister(this.registerId));
        this.context.alu.setB((short) 0);
    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.compare();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.context.isGreaterThan()) {
            // PC <- IAR
            this.context.setProgramCounter(this.context.getInternalAddressRegister());
            this.context.setDidBranch();
        }
    }

}

/**
 OPCODE 04 - Add Memory To Register
 Octal: 004
 AMR r, x, address[,I]
 r = 0..3
 r<- c(r) + c(EA)
 */
class AddMemoryToRegister extends RegisterMemoryInstruction {
    public AddMemoryToRegister(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(MAR)
        this.context.fetchMemoryAddressRegister();

        // A <- RX
        this.context.alu.setA(this.context.getGeneralRegister(this.registerId));
        // B <- MBR
        this.context.alu.setB(this.context.getMemoryBufferRegister());
    }

    public void execute(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // Y <- A + B
        this.context.alu.add();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // RX <- Y
        this.context.setGeneralRegister(this.registerId, this.context.alu.getYAsShort());
    }
}


/**
 OPCODE 05 - Subtract Memory From Register
 Octal: 005
 SMR r, x, address[,I]
 r = 0..3
 r<- c(r) – c(EA)
 */
class SubtractMemoryFromRegister extends RegisterMemoryInstruction {
    public SubtractMemoryFromRegister(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(MAR)
        this.context.fetchMemoryAddressRegister();

        // A <- RX
        this.context.alu.setA(this.context.getGeneralRegister(this.registerId));
        // B <- MBR
        this.context.alu.setB(this.context.getMemoryBufferRegister());
    }

    public void execute(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // Y <- A + B
        this.context.alu.subtract();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // RX <- Y
        this.context.setGeneralRegister(this.registerId, this.context.alu.getYAsShort());
    }
}

/**
 * OPCODE 06 - Add Immediate to Register
 * Octal: 006
 * AIR r, immed
 * r = 0..3
 * r <- c(r) + Immed
 * Note:
 *  1. if Immed = 0, does nothing
 *  2. if c(r) = 0, loads r with Immed
 *  IX and I are ignored in this instruction
 */
class AddImmediateToRegister extends RegisterMemoryInstruction {
    private short immediate;

    public AddImmediateToRegister(short word, Simulator context) {
        super(word, context);
        this.immediate = this.address;
    }

    public void fetchOperand(){
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.immediate == 0) return;

        // A <- RX
        this.context.alu.setA(this.context.getGeneralRegister(this.registerId));
        // B <- MBR
        this.context.alu.setB(this.immediate);
    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.immediate == 0) return;

        this.context.alu.add();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.immediate == 0) return;

        // RX <- Z
        this.context.setGeneralRegister(this.registerId, (short)this.context.alu.getYAsShort());
    }
}

/**
 OPCODE 07 - Subtract Immediate from Register
 Octal: 007
 SIR r, immed
 r = 0..3
 r <- c(r) - Immed
 Note:
 1. if Immed = 0, does nothing
 2. if c(r) = 0, loads r1 with –(Immed)
 IX and I are ignored in this instruction
 */
class SubtractImmediateFromRegister extends RegisterMemoryInstruction {
    private short immediate;

    public SubtractImmediateFromRegister(short word, Simulator context) {
        super(word, context);
        this.immediate = this.address;
    }

    public void fetchOperand(){
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.immediate == 0) return;

        // A <- RX
        this.context.alu.setA(this.context.getGeneralRegister(this.registerId));
        // B <- MBR
        this.context.alu.setB(this.immediate);
    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.immediate == 0) return;

        this.context.alu.subtract();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.immediate == 0) return;

        // RX <- Z
        this.context.setGeneralRegister(this.registerId, (short)this.context.alu.getYAsShort());
    }
}

