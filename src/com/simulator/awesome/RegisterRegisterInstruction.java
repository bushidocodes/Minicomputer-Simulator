package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by MLT, DVD, TRR, AND, ORR, NOT
public class RegisterRegisterInstruction extends Instruction {
    final public short firstRegisterId;
    final public short secondRegisterId;

    RegisterRegisterInstruction(short word, Simulator context) {
        super(word, context);
        short firstRegisterMask         = (short) 0b0000001100000000;
        short secondRegisterMask        = (short) 0b0000000011000000;

        short secondRegisterOffset      = 6;
        short firstRegisterOffset       = 8;

        // Initialize the register Ids.
        this.secondRegisterId = Utils.short_unsigned_right_shift((short)(word & secondRegisterMask), secondRegisterOffset);
        this.firstRegisterId = Utils.short_unsigned_right_shift((short)(word & firstRegisterMask), firstRegisterOffset);

        // Since we declared firstRegisterId and secondRegisterId as final, we can just validate once here.
        this.validateGeneralRegisterIndex(this.firstRegisterId);
        this.validateGeneralRegisterIndex(this.secondRegisterId);
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
        // Fault Handling and Validation
        if (this.firstRegisterId != 0 && this.firstRegisterId != 2)  this.didFault = true;
        if (this.didFault) return;

        // A <- RX
        this.context.alu.setA(this.context.getGeneralRegister(this.firstRegisterId));
        // B <- RY
        this.context.alu.setB(this.context.getGeneralRegister(this.secondRegisterId));
    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.multiply();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // Returns a 2-tuple of shorts containing the high and low bits
        short[] results = this.context.alu.getYAsShorts();

        // rx <- top bits of z
        this.context.setGeneralRegister(this.firstRegisterId, results[0]);
        // rx + 1 <- bottom bits of z
        this.context.setGeneralRegister((short)(this.firstRegisterId + 1), results[1]);
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
        // Fault Handling and Validation
        if (this.firstRegisterId != 0 && this.firstRegisterId != 2) this.didFault = true;
        if (this.didFault) return;

        this.context.alu.setA(this.context.getGeneralRegister(this.firstRegisterId));
        this.context.alu.setB(this.context.getGeneralRegister(this.secondRegisterId));
    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.divide();

        if (this.context.cc.isDivideByZero()) {
            this.didFault = true;
        }
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // RX <- Y
        this.context.setGeneralRegister(this.firstRegisterId, (short)(this.context.alu.getYAsShort()));
        // RX+1 <- Y2
        this.context.setGeneralRegister((short)(this.firstRegisterId + 1), (short)(this.context.alu.getY2AsShort()));
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
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.setA(this.context.getGeneralRegister(this.firstRegisterId));
        this.context.alu.setB(this.context.getGeneralRegister(this.secondRegisterId));
    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.compare();
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
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.setA(this.context.getGeneralRegister(this.firstRegisterId));
        this.context.alu.setB(this.context.getGeneralRegister(this.secondRegisterId));
    }


    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.and();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.setGeneralRegister(this.firstRegisterId, this.context.alu.getYAsShort());
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
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.setA(this.context.getGeneralRegister(this.firstRegisterId));
        this.context.alu.setB(this.context.getGeneralRegister(this.secondRegisterId));
    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.or();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.setGeneralRegister(this.firstRegisterId, this.context.alu.getYAsShort());
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
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.setA(this.context.getGeneralRegister(this.firstRegisterId));
    }

    public void execute() {
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.alu.not();
    }

    public void storeResult(){
        // Fault Handling and Validation
        if (this.didFault) return;

        this.context.setGeneralRegister(this.firstRegisterId, this.context.alu.getYAsShort());
    }
}
