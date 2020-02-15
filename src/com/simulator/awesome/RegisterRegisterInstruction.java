package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by MLT, DVD, TRR, AND, ORR, NOT
public class RegisterRegisterInstruction extends Instruction {
    public short firstRegisterId;
    public short secondRegisterId;

    RegisterRegisterInstruction(short word, Simulator context) {
        super(word, context);
        short firstRegisterMask         = (short) 0b0000001100000000;
        short secondRegisterMask        = (short) 0b0000000011000000;

        short secondRegisterOffset      = 6;
        short firstRegisterOffset       = 8;

        this.secondRegisterId = Utils.short_unsigned_right_shift((short)(word & secondRegisterMask), secondRegisterOffset);
        this.secondRegisterId = Utils.short_unsigned_right_shift((short)(word & firstRegisterMask), firstRegisterOffset);
    }

    public void fetchOperand(){
        // NOOP
    }

    public void execute() {
        // NOOP
    }

    public void storeResult(){
        // NOOP
    }

    public void print(){
        System.out.println("OpCode: " + this.opCode);
        System.out.println("First Register ID: " + this.firstRegisterId);
        System.out.println("Second Register ID: " + this.secondRegisterId);
    }
}


/**
 OPCODE 20 - Multiply Register by Register
 octal: 024
 MLT rx,ry
 rx, rx+1 <- c(rx) * c(ry)
 rx must be 0 or 2
 ry must be 0 or 2
 rx contains the high order bits, rx+1 contains the low order bits of the result
 Set OVERFLOW flag, if overflow
 */
class MultiplyRegisterByRegister extends RegisterRegisterInstruction {
    public MultiplyRegisterByRegister(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        if (this.firstRegisterId == 0 || this.firstRegisterId == 2) {
            // IAR <- RX
            this.context.setInternalAddressRegister(this.context.getGeneralRegister(this.firstRegisterId));
            // MAR <- IAR
            this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
            // MBR <- c(RX)
            this.context.fetchMemoryAddressRegister();
            // y <- MBR
            this.context.setY(this.context.getMemoryBufferRegister());
        } else {
            // TODO: Set some sort of machine fault because the registers were not valid values
        }
    }

    public void execute() {
        if (this.secondRegisterId == 0 || this.secondRegisterId == 2) {
            // IAR <- RY
            this.context.setInternalAddressRegister(this.context.getIndexRegister(this.secondRegisterId));
            // MAR <- IAR
            this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
            // MBR <- c(RY)
            this.context.fetchMemoryAddressRegister();
            // z <- y * MBR
            this.context.setZ(this.context.getY() * this.context.getMemoryBufferRegister());
        } else {
            // TODO: Set some sort of machine fault because the registers were not valid values
        }
    }

    public void storeResult(){
        // TODO: How can we guarantee that we interrupt the "steps" when we cause a fault?
        // rx <- top bits of z
        this.context.setGeneralRegister(this.firstRegisterId, (short)(this.context.getZ() >>> 16));
        // rx + 1 <- bottom bits of z
        this.context.setGeneralRegister((short)(this.firstRegisterId + 1), (short)(this.context.getZ() << 16 >>> 16));
    }
}

/**
 OPCODE 21 - Divide Register by Register
 Octal: 025
 DVD rx,ry
 rx, rx+1 <- c(rx)/ c(ry)
 rx must be 0 or 2
 rx contains the quotient; rx+1 contains the remainder
 ry must be 0 or 2
 If c(ry) = 0, set cc(3) to 1 (set DIVZERO flag)
 */
class DivideRegisterByRegister extends RegisterRegisterInstruction {
    public DivideRegisterByRegister(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        if (this.firstRegisterId == 0 || this.firstRegisterId == 2) {
            // IAR <- RX
            this.context.setInternalAddressRegister(this.context.getGeneralRegister(this.firstRegisterId));
            // MAR <- IAR
            this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
            // MBR <- c(RX)
            this.context.fetchMemoryAddressRegister();
            // y <- MBR
            this.context.setY(this.context.getMemoryBufferRegister());
        } else {
            // TODO: Set some sort of machine fault because the registers were not valid values
        }
    }

    public void execute() {
        if (this.secondRegisterId == 0 || this.secondRegisterId == 2) {
            // IAR <- RY
            this.context.setInternalAddressRegister(this.context.getIndexRegister(this.secondRegisterId));
            // MAR <- IAR
            this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
            // MBR <- c(RY)
            this.context.fetchMemoryAddressRegister();
            // z <- y / MBR
            this.context.setZ(this.context.getY() / this.context.getMemoryBufferRegister());
            // rx <- z
            this.context.setGeneralRegister(this.firstRegisterId, (short)(this.context.getZ()));
        } else {
            // TODO: Set some sort of machine fault because the registers were not valid values
        }
    }

    public void storeResult(){
        // TODO: How can we guarantee that we interrupt the "steps" when we cause a fault?
        // z <- y % MBR
        this.context.setZ(this.context.getY() % this.context.getMemoryBufferRegister());
        // rx+1 <- z
        this.context.setGeneralRegister((short)(this.firstRegisterId + 1), (short)(this.context.getZ()));
    }
}

/**
 OPCODE 22 - Test the Equality of Register and Register
 Octal: 026
 TRR rx, ry
 If c(rx) = c(ry), set cc(4) <- 1; else, cc(4) <- 0
 */
class TestTheEqualityOfRegisterAndRegister extends RegisterRegisterInstruction {
    public TestTheEqualityOfRegisterAndRegister(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        // IAR <- RX
        this.context.setInternalAddressRegister(this.context.getGeneralRegister(this.firstRegisterId));
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(RX)
        this.context.fetchMemoryAddressRegister();
        // y <- MBR
        this.context.setY(this.context.getMemoryBufferRegister());
    }

    public void execute() {
        // IAR <- RX
        this.context.setInternalAddressRegister(this.context.getGeneralRegister(this.secondRegisterId));
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(RX)
        this.context.fetchMemoryAddressRegister();
        // compare y and MBR (I'm skipping setting the z register to avoid casting from boolean to int)
        this.context.setEqualOrNot(this.context.getY() == this.context.getMemoryBufferRegister());
    }

    public void storeResult(){
        // NOOP
    }
}

/**
 OPCODE 23 - Logical And of Register and Register
 Octal: 027
 AND rx, ry
 c(rx) <- c(rx) AND c(ry)
 */
class LogicalAndOfRegisterAndRegister extends RegisterRegisterInstruction {
    public LogicalAndOfRegisterAndRegister(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        // IAR <- RX
        this.context.setInternalAddressRegister(this.context.getGeneralRegister(this.firstRegisterId));
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(RX)
        this.context.fetchMemoryAddressRegister();
        // y <- MBR
        this.context.setY(this.context.getMemoryBufferRegister());
    }

    public void execute() {
        // IAR <- RX
        this.context.setInternalAddressRegister(this.context.getGeneralRegister(this.secondRegisterId));
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(RX)
        this.context.fetchMemoryAddressRegister();
        // z <- y & MBR
        this.context.setZ(this.context.getY() & this.context.getMemoryBufferRegister());
    }

    public void storeResult(){
        this.context.setGeneralRegister(this.firstRegisterId, (short)(this.context.getZ() << 16 >>> 16));
    }
}

/**
 OPCODE 24 - Logical Or of Register and Register
 Octal: 030
 ORR rx, ry
 c(rx) <- c(rx) OR c(ry)
 */
class LogicalOrOfRegisterAndRegister extends RegisterRegisterInstruction {
    public LogicalOrOfRegisterAndRegister(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        // IAR <- RX
        this.context.setInternalAddressRegister(this.context.getGeneralRegister(this.firstRegisterId));
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(RX)
        this.context.fetchMemoryAddressRegister();
        // y <- MBR
        this.context.setY(this.context.getMemoryBufferRegister());
    }

    public void execute() {
        // IAR <- RX
        this.context.setInternalAddressRegister(this.context.getGeneralRegister(this.secondRegisterId));
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(RX)
        this.context.fetchMemoryAddressRegister();
        // z <- y | MBR
        this.context.setZ(this.context.getY() | this.context.getMemoryBufferRegister());
    }

    public void storeResult(){
        this.context.setGeneralRegister(this.firstRegisterId, (short)(this.context.getZ() << 16 >>> 16));
    }
}

/**
 OPCODE 25 - Logical Not of Register To Register
 Octal: 031
 NOT rx
 C(rx) <- NOT c(rx)
 */
class LogicalNotOfRegisterAndRegister extends RegisterRegisterInstruction {
    public LogicalNotOfRegisterAndRegister(short word, Simulator context) {
        super(word, context);
    }

    public void fetchOperand(){
        // IAR <- RX
        this.context.setInternalAddressRegister(this.context.getGeneralRegister(this.firstRegisterId));
        // MAR <- IAR
        this.context.setMemoryAddressRegister(this.context.getInternalAddressRegister());
        // MBR <- c(RX)
        this.context.fetchMemoryAddressRegister();
        // y <- MBR
        this.context.setY(this.context.getMemoryBufferRegister());
    }

    public void execute() {
        // z <- ~y
        this.context.setZ(~this.context.getY());
    }

    public void storeResult(){
        this.context.setGeneralRegister(this.firstRegisterId, (short)(this.context.getZ() << 16 >>> 16));
    }
}
