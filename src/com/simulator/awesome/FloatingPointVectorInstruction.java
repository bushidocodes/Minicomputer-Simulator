package com.simulator.awesome;

public class FloatingPointVectorInstruction extends Instruction {
    final public short floatingRegisterId;
    final public short indexRegisterId;   // Acts as base address
    final public boolean isIndirect;
    final public short address;           // Acts as offset

    FloatingPointVectorInstruction(short word, Simulator context) {
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
        this.floatingRegisterId = Utils.short_unsigned_right_shift((short)(word & registerMask), registerOffset);

        this.validateFloatingRegisterIndex(this.floatingRegisterId);
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
        System.out.println("Floating Register ID: " + this.floatingRegisterId);
        System.out.println("Index Register ID: " + this.indexRegisterId);
        System.out.println("Is Indirect? : " + this.isIndirect);
        System.out.println("Address: " + this.address);
    }
}

/**
 OPCODE 33 - Floating Add Memory To Register
 Octal: 041
 FADD fr, x, address[,I]
 c(fr) <- c(fr) + c(EA)
 c(fr) <- c(fr) + c(c(EA)), if I bit set
 fr must be 0 or 1.
 OVERFLOW may be set
 */
class FloatingAddMemoryToRegister extends FloatingPointVectorInstruction {
    public FloatingAddMemoryToRegister(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        // A <- FRX
        this.context.fpu.setA(this.context.getFloatingRegister(this.floatingRegisterId));

        // MAR <- IAR
        // MBR <- c(MAR)
        // B <- MBR
        this.context.fpu.setB(this.context.memory.fetch(this.context.getInternalAddressRegister()));
    }

    public void execute(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // Y <- A + B
        this.context.fpu.add();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // FPX <- Y
        this.context.setFloatingRegister(this.floatingRegisterId, this.context.fpu.getYAsShort());
    }
}

/**
 OPCODE 34 - Floating Subtract Memory From Register
 Octal: 042
 FSUB fr, x, address[,I]
 c(fr) <- c(fr) - c(EA)
 c(fr) <- c(fr) - c(c(EA)), if I bit set
 fr must be 0 or 1
 UNDERFLOW may be set
 */
class FloatingSubtractMemoryFromRegister extends FloatingPointVectorInstruction {
    public FloatingSubtractMemoryFromRegister(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        // A <- FPX
        this.context.fpu.setA(this.context.getFloatingRegister(this.floatingRegisterId));

        // MAR <- IAR
        // MBR <- c(MAR)
        // B <- MBR
        this.context.fpu.setB(this.context.memory.fetch(this.context.getInternalAddressRegister()));
    }

    public void execute(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // Y <- A - B
        this.context.fpu.subtract();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // FPX <- Y
        this.context.setFloatingRegister(this.floatingRegisterId, this.context.fpu.getYAsShort());
    }
}

/**
 OPCODE 37 - Convert to Fixed/FloatingPoint
 Octal: 045
 CNVRT r, x, address[,I]
 If F = 0, convert c(EA) to a fixed point number and store in r.
 If F = 1, convert c(EA) to a floating point number and store in FR0.
 The r register contains the value of F before the instruction is executed.
 */
class ConvertToFixedOrFloatingPoint extends FloatingPointVectorInstruction {
    public ConvertToFixedOrFloatingPoint(short word, Simulator context) {
        super(word, context);
        // There is a reference to a general register in the normal floating register field. This is not a bug!
        this.validateGeneralRegisterIndex(this.floatingRegisterId);
        this.validateIndexRegisterIndex(this.indexRegisterId);
    }

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Fault Handling and Validation
        if (this.didFault) return;

        // IAR <- EA
        computeEffectiveAddress();
        if (this.isIndirect) this.evaluatePointerToAddress();

        if (this.floatingRegisterId == 0){
            // Convert Floating -> Fixed
            this.context.fpu.setConversionType(0);

            // MAR <- IAR
            // MBR <- c(MAR)
            // A <- MBR
            this.context.fpu.setA(this.context.memory.fetch(this.context.getInternalAddressRegister()));
        } else if (this.floatingRegisterId == 1){
            // Convert Fixed -> Floating
            this.context.fpu.setConversionType(1);

            // MAR <- IAR
            // MBR <- c(MAR)
            // fixed <- MBR
            this.context.fpu.setFixed(this.context.memory.fetch(this.context.getInternalAddressRegister()));
        }
    }

    public void execute(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // Convert fixed to floating point or vice versa
        this.context.fpu.convert();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        if(this.context.fpu.getConversionType() == 0){
            // 0 = Floating -> Fixed
            // Store the value in the r
            this.context.setGeneralRegister(this.floatingRegisterId, this.context.fpu.getYAsShort());
        } else if (this.context.fpu.getConversionType() == 1){
            // 1 = Fixed -> Floating
            // Store the value in FR1
            this.context.setFloatingRegister(this.floatingRegisterId, this.context.fpu.getYAsShort());
            // R1 contains the value of F before the instruction is executed
            this.context.setGeneralRegister(this.floatingRegisterId, this.context.fpu.getFixed());

        }
    }
}

/**
 OPCODE 50 - Load Floating Register From Memory
 Octal: 062
 LDFR fr, x, address [,i]
 fr = 0..1
 fr <- c(EA), c(EA+1)
 fr <- c(c(EA), c(EA)+1), if I bit set
 */
class LoadFloatingPointFromMemory extends FloatingPointVectorInstruction {
    public LoadFloatingPointFromMemory(short word, Simulator context) {
        super(word, context);
        this.validateFloatingRegisterIndex(this.floatingRegisterId);
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
        this.context.setFloatingRegister(this.floatingRegisterId, this.context.memory.fetch(this.context.getInternalAddressRegister()));
    }
}

/**
 OPCODE 51 - Store Floating Register To Memory
 Octal: 063
 STFR fr, x, address [,i]
 fr = 0..1
 EA, EA+1 <- c(fr)
 c(EA), c(EA)+1 <- c(fr), if I-bit set
 */
class StoreFloatingPointToMemory extends FloatingPointVectorInstruction {
    public StoreFloatingPointToMemory(short word, Simulator context) {
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
        this.context.memory.store(this.context.getInternalAddressRegister(), this.context.getFloatingRegister(this.floatingRegisterId));
    }
}
