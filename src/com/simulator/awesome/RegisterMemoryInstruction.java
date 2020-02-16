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

        // MAR <- EA
        super.fetchOperand();
        // MBR <- c(MAR)
        this.context.fetchMemoryAddressRegister();
    }

    public void execute(){
        // NOOP
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // RX <- MBR
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
class StoreRegisterToMemory extends RegisterMemoryInstruction {
    public StoreRegisterToMemory(short word, Simulator context) {
        super(word, context);
    }

    // Default fetchOperand hook
    // MAR <- EA

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        // MBR <- RX
        this.context.setMemoryBufferRegister(this.context.getGeneralRegister(this.registerId));
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // c(MAR) <- MBR
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
class LoadRegisterWithAddress extends RegisterMemoryInstruction {
    public LoadRegisterWithAddress(short word, Simulator context) {
        super(word, context);
    }

    // Default fetchOperand hook
    // MAR <- EA

    public void execute(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // NOOP
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // RX <- MAR
        this.context.setGeneralRegister(this.registerId, this.context.getMemoryAddressRegister());
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

    // Default fetchOperand hook
    // MAR <- EA

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        // MBR <- c(MAR)
        this.context.fetchMemoryAddressRegister();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // X0 <- MBR
        this.context.setIndexRegister(this.destinationIndexRegisterId, this.context.getMemoryBufferRegister());
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

    // Default fetchOperand hook
    // MAR <- EA

    // Copies the index register value to the MBR
    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        // MBR <- X0
        this.context.setMemoryBufferRegister(this.context.getIndexRegister(this.sourceIndexRegisterId));
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // c(MAR) <- MBR
        this.context.setWord(this.context.getMemoryAddressRegister(), this.context.getMemoryBufferRegister());
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

        if (this.context.getGeneralRegister(this.registerId) == 0) {
            // IAR <- EA
            computeEffectiveAddress();
            if (this.isIndirect) this.evaluatePointerToAddress();
        }
    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.context.getGeneralRegister(this.registerId) == 0) {
            // PC <- IAR
            this.context.setProgramCounter(this.context.getInternalAddressRegister());
            this.context.setDidBranch();
        }
    }

    public void storeResult(){
        // NOOP
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

        if (this.context.getGeneralRegister(this.registerId) == 0) {
            // IAR <- EA
            this.computeEffectiveAddress();
            if (this.isIndirect) this.evaluatePointerToAddress();
        }
    }

    public void execute() {
        if (this.context.getGeneralRegister(this.registerId) != 0) {
            // PC <- IAR
            this.context.setProgramCounter(this.context.getInternalAddressRegister());
            this.context.setDidBranch();
        }
    }

    public void storeResult(){
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
        }
    }

    public void execute() {
        if (this.context.isCondition(this.conditionCode)){
            this.context.setProgramCounter(this.context.getInternalAddressRegister());
            this.context.setDidBranch();
        }
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
    }

    public void execute() {
        this.context.setProgramCounter(this.context.getInternalAddressRegister());
        this.context.setDidBranch();
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
    }

    public void execute() {
        // R3 <- PC+1
        this.context.setGeneralRegister((short) 3, (short) (this.context.getProgramCounter() + 1));
    }

    public void storeResult(){
        // PC <- IAR
        this.context.setProgramCounter(this.context.getInternalAddressRegister());
        this.context.setDidBranch();
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
        // NOOP
    }

    public void execute() {
        this.context.setGeneralRegister((short) 0, this.immediateValue);
    }

    public void storeResult() {
        this.context.setProgramCounter(this.context.getGeneralRegister((short) 3));
        this.context.setDidBranch();
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
    }

    public void execute() {
        this.context.setGeneralRegister(this.registerId, (short)(this.context.getGeneralRegister(this.registerId) - 1));
    }

    public void storeResult(){
        if (this.context.getGeneralRegister(this.registerId) > 0){
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
    }

    public void execute() {
        if (this.context.getGeneralRegister(this.registerId) >= 0){
            this.context.setProgramCounter(this.context.getInternalAddressRegister());
            this.context.setDidBranch();
        }
    }

    public void storeResult(){
        // NOOP
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
        super.fetchOperand();
        // Fetch the contents of the word in memory specified by the MAR into the MBR.
        this.context.fetchMemoryAddressRegister();
    }

    public void execute(){
        this.context.setGeneralRegister(this.registerId, (short)(this.context.getGeneralRegister(this.registerId) + this.context.getMemoryBufferRegister()));
    }

    public void storeResult(){
        // NOOP
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
        super.fetchOperand();
        // Fetch the contents of the word in memory specified by the MAR into the MBR.
        this.context.fetchMemoryAddressRegister();
    }

    public void execute(){
        this.context.setGeneralRegister(this.registerId, (short)(this.context.getGeneralRegister(this.registerId) - this.context.getMemoryBufferRegister()));
    }

    public void storeResult(){
        // NOOP
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
        // NOOP
    }

    public void execute() {
        if (this.immediate == 0) return;
        this.context.setGeneralRegister(this.registerId, (short)(this.context.getGeneralRegister(this.registerId) + this.immediate));
    }

    public void storeResult(){
        // NOOP
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
        // NOOP
    }

    public void execute() {
        if (this.immediate == 0) return;
        this.context.setGeneralRegister(this.registerId, (short)(this.context.getGeneralRegister(this.registerId) - this.immediate));
    }

    public void storeResult(){
        // NOOP
    }
}

