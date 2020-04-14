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

    public void evaluatePointerToAddress() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // MAR <- IAR (storing address of pointer)
        // MBR <- c(MAR)
        // IAR <- MBR
        this.context.setInternalAddressRegister(this.context.memory.fetch(this.context.getInternalAddressRegister()));
    }

    // MAR <- EA
    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

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

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // MAR <- IAR
        // MBR <- c(MAR)
        // RX <- MBR
        this.context.setGeneralRegister(this.registerId, this.context.memory.fetch(this.context.getInternalAddressRegister()));
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

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        // MAR <- IAR
        // MBR <- RX
        // c(MAR) <- MBR
        this.context.memory.store(this.context.getInternalAddressRegister(), this.context.getGeneralRegister(this.registerId));
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

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        // Note: If this is an indirect, this ends up being a memory seek with more than one cycle
        // Disregarding this for simplicity
        if (this.isIndirect) this.evaluatePointerToAddress();
        // RX <- IAR
        this.context.setGeneralRegister(this.registerId, this.context.getInternalAddressRegister());
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

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // MAR <- IAR
        // MBR <- c(MAR)
        // X0 <- MBR
        this.context.setIndexRegister(this.destinationIndexRegisterId, this.context.memory.fetch(this.context.getInternalAddressRegister()));
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

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        // MAR <- IAR
        // MBR <- X0
        // c(MAR) <- MBR
        this.context.memory.store(this.context.getInternalAddressRegister(), this.context.getIndexRegister(this.sourceIndexRegisterId));
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

    public void storeResult() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.context.cc.isEqual()) {
            // IAR <- EA
            computeEffectiveAddress();
            if (this.isIndirect) this.evaluatePointerToAddress();
            // PC <- IAR
            this.context.pc.set(this.context.getInternalAddressRegister());
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
    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        this.computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

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

    public void storeResult() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        if (!this.context.cc.isEqual()) {
            // IAR <- EA
            computeEffectiveAddress();
            if (this.isIndirect) this.evaluatePointerToAddress();
            // PC <- IAR
            this.context.pc.set(this.context.getInternalAddressRegister());
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
    private final int conditionCode;

    public JumpIfConditionCode(short word, Simulator context) {
        super(word, context);
        this.conditionCode = this.registerId;
    }

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.context.cc.isCondition(this.conditionCode)){
            // IAR <- EA
            this.computeEffectiveAddress();
            if (this.isIndirect) this.evaluatePointerToAddress();
            this.context.pc.set(this.context.getInternalAddressRegister());
        }
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

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        this.computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // PC <- IAR
        this.context.pc.set(this.context.getInternalAddressRegister());
    }

}


//TODO: I have not idea about the comment below about the argument list. Where do we store the arguments we're passing to the subroutine?

/**
 OPCODE 14 - Jump and Save Return Address
 Octal: 016
 JSR x, address[,I]
 R3 <- PC+1;
 IX3 <- EA (added to spec to allow the subroutine to use relative addresses)
 PC <- EA
 R0 should contain pointer to arguments
 */
class JumpAndSaveReturnAddress extends RegisterMemoryInstruction {
    public JumpAndSaveReturnAddress(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        this.computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();
        // R3 <- PC (we've already incremented PC at this point)
        this.context.setGeneralRegister((short) 3, this.context.pc.get());

        // Increment the call stack. Setting Base address to first address of stack frame to base address of function
        this.context.incrementCallStack(this.context.getInternalAddressRegister());
        // IX3 <- IAR. TODO: Refactor all functions to not use this
        this.context.setIndexRegister((short) 3, this.context.getInternalAddressRegister());
        // PC <- IAR
        this.context.pc.set(this.context.getInternalAddressRegister());
    }

}

/**
 OPCODE 15 - Return From Subroutine
 Octal: 017
 w/ return code as Immediate portion (optional) stored in the instruction’s address field.
 RFS Immediate
 R0 <- Immediate; PC <- c(R3)
 IX, I fields are ignored.
 */
class ReturnFromSubroutine extends RegisterMemoryInstruction {
    final short immediateValue;

    public ReturnFromSubroutine(short word, Simulator context) {
        super(word, context);
        this.immediateValue = this.address;
    }

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.setGeneralRegister((short) 0, this.immediateValue);

        // We are using RFS as a "trap return" and "fault return" to avoid having to create a new function
        // If we wanted to be able to call subroutines in traps, we would have to implemented a RFT (return from trap) OPCODE
        if (this.context.msr.isExecutingFaultHandler()) {
            // "Fault Handler" Exit
            this.context.pc.set(this.context.memory.fetch((short)4));
            if (this.context.msr.isSupervisorFault()){
                this.context.msr.setIsSupervisorFault(false);
            } else {
                this.context.msr.setSupervisorMode(false);
            }
            this.context.msr.setIsExecutingFaultHandler(false);

            // Clear all MFR registers
            this.context.mfr.setIsIllegalMemoryAddressBeyondLimit(false);
            this.context.mfr.setIllegalMemoryAccessToReservedLocations(false);
            this.context.mfr.setIllegalTrapCode(false);
            this.context.mfr.setIsIllegalOpcode(false);
        } else if (this.context.msr.isSupervisorMode()){
            // "Trap Handler" Exit
            // Decrement Call Stack
            this.context.decrementCallStack();
            // Restore PC to value stored in Address 2
            this.context.pc.set(this.context.memory.fetch((short)2));
            // The spec mentions saving and restoring the MSR, but there is no clear need for this currently based on
            // the sorts of state we store in the MSR
            this.context.msr.setSupervisorMode(false);
        } else {
            // Normal Subroutine Call Exit
            this.context.decrementCallStack();
            this.context.pc.set(this.context.getGeneralRegister((short) 3));
        }
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

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

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
        this.context.setGeneralRegister(this.registerId,this.context.alu.getYAsShort());
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        if (this.context.cc.isGreaterThan()){
            // PC <- IAR
            this.context.pc.set(this.context.getInternalAddressRegister());
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

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

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

        if (this.context.cc.isGreaterThan() || (this.context.cc.isEqual() && this.context.alu.getAAsInt() == 0)) {
            // PC <- IAR
            this.context.pc.set(this.context.getInternalAddressRegister());
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

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        // A <- RX
        this.context.alu.setA(this.context.getGeneralRegister(this.registerId));

        // MAR <- IAR
        // MBR <- c(MAR)
        // B <- MBR
        this.context.alu.setB(this.context.memory.fetch(this.context.getInternalAddressRegister()));
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

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        // A <- RX
        this.context.alu.setA(this.context.getGeneralRegister(this.registerId));

        // MAR <- IAR
        // MBR <- c(MAR)
        // B <- MBR
        this.context.alu.setB(this.context.memory.fetch(this.context.getInternalAddressRegister()));
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
    private final short immediate;

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
        this.context.setGeneralRegister(this.registerId, this.context.alu.getYAsShort());
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
    private final short immediate;

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
        this.context.setGeneralRegister(this.registerId, this.context.alu.getYAsShort());
    }
}

/**
 OPCODE 35 - Vector Add
 Octal: 043
 VADD fr, x, address[,I]
 fr contains the length of the vectors
 c(EA) or c(c(EA)), if I bit set, is address of first vector
 c(EA+1) or c(c(EA+1)), if I bit set, is address of the second vector
 Let V1 be vector at address; Let V2 be vector at address+1
 Then, V1[i] = V1[i]+ V2[i], i = 1, c(fr).
 */
class VectorAdd extends RegisterMemoryInstruction {
    final public short vectorLength;
    private short a[];
    private short b[];
    private short y[];

    public VectorAdd(short word, Simulator context) {
        super(word, context);
        this.vectorLength = this.registerId;
        this.a = new short[4];
        this.b = new short[4];
        this.y = new short[4];
    }
    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        // Add left hand vector to a[]
        for (int i = 0; i < this.vectorLength; i++){
            a[i] = this.context.memory.fetch((short)(this.context.getInternalAddressRegister() + i));
        }
        // Add right hand vector to b[]
        for (int i = 0; i < this.vectorLength; i++){
            b[i] = this.context.memory.fetch((short)(this.context.getInternalAddressRegister() + this.vectorLength + i));
        }
    }

    public void execute(){
        // Fault Handling and Validation
        if (this.didFault) return;

        for (int i = 0; i < this.vectorLength; i++){
            y[i] = (short)(a[i] + b[i]);
        }
    }

    public void storeResult() throws IllegalMemoryAddressBeyondLimitException, IllegalMemoryAccessToReservedLocationsException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        // Overwrite left hand vector with y[]
        for (int i = 0; i < this.vectorLength; i++){
            this.context.memory.store((short)(this.context.getInternalAddressRegister() + i), y[i]);
        }
    }

}

/**
 OPCODE 36 - Vector Subtract
 Octal: 044
 VSUB fr, x, address[,I]
 fr contains the length of the vectors
 c(EA) or c(c(EA)), if I bit set is address of first vector
 c(EA+1) or c(c(EA+1)), if I bit set is address of the second vector
 Let V1 be vector at address; Let V2 be vector at address+1
 Then, V1[i] = V1[i] - V2[i], i = 1, c(fr).
 */
class VectorSubtract extends RegisterMemoryInstruction {
    final public short vectorLength;
    private short a[];
    private short b[];
    private short y[];

    public VectorSubtract(short word, Simulator context) {
        super(word, context);
        this.vectorLength = this.registerId;
        this.a = new short[4];
        this.b = new short[4];
        this.y = new short[4];
    }
    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        // Add left hand vector to a[]
        for (int i = 0; i < this.vectorLength; i++){
            a[i] = this.context.memory.fetch((short)(this.context.getInternalAddressRegister() + i));
        }
        // Add right hand vector to b[]
        for (int i = 0; i < this.vectorLength; i++){
            b[i] = this.context.memory.fetch((short)(this.context.getInternalAddressRegister() + this.vectorLength + i));
        }
    }

    public void execute(){
        // Fault Handling and Validation
        if (this.didFault) return;

        for (int i = 0; i < this.vectorLength; i++){
            y[i] = (short)(a[i] - b[i]);
        }
    }

    public void storeResult() throws IllegalMemoryAddressBeyondLimitException, IllegalMemoryAccessToReservedLocationsException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        // Overwrite left hand vector with y[]
        for (int i = 0; i < this.vectorLength; i++){
            this.context.memory.store((short)(this.context.getInternalAddressRegister() + i), y[i]);
        }
    }


}