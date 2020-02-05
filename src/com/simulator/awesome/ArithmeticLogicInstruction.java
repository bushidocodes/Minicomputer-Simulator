package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by MLT, DVD, TRR, AND, ORR, NOT
public class ArithmeticLogicInstruction extends Instruction {
    public short firstRegisterId;
    public short secondRegisterId;

    ArithmeticLogicInstruction(short word, Simulator context) {
        super(word, context);
        short firstRegisterMask         = (short) 0b0000001100000000;
        short secondRegisterMask        = (short) 0b0000000011000000;

        short secondRegisterOffset      = 6;
        short firstRegisterOffset       = 8;

        this.secondRegisterId = (short)((word & secondRegisterMask) >>> secondRegisterOffset);
        this.firstRegisterId = (short)((word & firstRegisterMask) >>> firstRegisterOffset);
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

class AddMemoryToRegister extends ArithmeticLogicInstruction {
    public AddMemoryToRegister(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 04 - Add Memory To Register
     Octal: 004
     AMR r, x, address[,I]
     r = 0..3
     r<- c(r) + c(EA)
     */
    public void execute() {
        System.out.println("AMR");
    }

    public void storeResult(){
        // NOOP
    }
}


class SubtractMemoryFromRegister extends ArithmeticLogicInstruction {
    public SubtractMemoryFromRegister(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 05 - Subtract Memory From Register
     Octal: 005
     SMR r, x, address[,I]
     r = 0..3
     r<- c(r) – c(EA)
     */
    public void execute() {
        System.out.println("SMR");
    }

    public void storeResult(){
        // NOOP
    }
}

class AddImmediateToRegister extends ArithmeticLogicInstruction {
    public AddImmediateToRegister(short word, Simulator context) {
        super(word, context);
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
    public void execute() {
        System.out.println("AIR");
    }

    public void storeResult(){
        // NOOP
    }
}

class SubtractImmediateFromRegister extends ArithmeticLogicInstruction {
    public SubtractImmediateFromRegister(short word, Simulator context) {
        super(word, context);
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
    public void execute() {
        System.out.println("SIR");
    }

    public void storeResult(){
        // NOOP
    }
}

class JumpIfZero extends ArithmeticLogicInstruction {
    public JumpIfZero(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 10 - Jump If Zero
     Octal: 012
     JZ r, x, address[,I]
     If c(r) = 0, then PC <- EA
     Else PC <- PC+1
     */
    public void execute() {
        System.out.println("JZ");
    }

    public void storeResult(){
        // NOOP
    }
}

class JumpIfNotEqual extends ArithmeticLogicInstruction {
    public JumpIfNotEqual(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 11 - Jump If Not Equal
     Octal: 013
     JNE r, x, address[,I]
     If c(r) != 0, then PC <-- EA
     Else PC <- PC + 1
     */
    public void execute() {
        System.out.println("JNE");
    }

    public void storeResult(){
        // NOOP
    }
}

class JumpIfConditionCode extends ArithmeticLogicInstruction {
    public JumpIfConditionCode(short word, Simulator context) {
        super(word, context);
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
    public void execute() {
        System.out.println("JCC");
    }

    public void storeResult(){
        // NOOP
    }
}

class UnconditionalJumpToAddress extends ArithmeticLogicInstruction {
    public UnconditionalJumpToAddress(short word, Simulator context) {
        super(word, context);
    }

    /**
     * OPCODE 13 - Unconditional Jump To Address
     * Octal: 015
     * JMA x, address[,I]
     * PC <- EA,
     * Note: r is ignored in this instruction
     */
    public void execute() {
        System.out.println("JMA");
    }

    public void storeResult(){
        // NOOP
    }
}

class JumpAndSaveReturnAddress extends ArithmeticLogicInstruction {
    public JumpAndSaveReturnAddress(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 14 - Jump and Save Return Address
     Octal: 016
     JSR x, address[,I]
     R3 <- PC+1;
     PC <- EA
     R0 should contain pointer to arguments
     Argument list should end with –1 (all 1s) value
     */
    public void execute() {
        System.out.println("JSR");
    }

    public void storeResult(){
        // NOOP
    }
}

class ReturnFromSubroutine extends ArithmeticLogicInstruction {
    public ReturnFromSubroutine(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 15 - Return From Subroutine
     Octal: 017
     w/ return code as Immed portion (optional) stored in the instruction’s address field.
     RFS Immed
     R0 <- Immed; PC <- c(R3)
     IX, I fields are ignored.
     */
    public void execute() {
        System.out.println("RFS");
    }

    public void storeResult(){
        // NOOP
    }
}

class SubtractOneAndBranch extends ArithmeticLogicInstruction {
    public SubtractOneAndBranch(short word, Simulator context) {
        super(word, context);
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
    public void execute() {
        System.out.println("SOB");
    }

    public void storeResult(){
        // NOOP
    }
}

class JumpGreaterThanOrEqualTo extends ArithmeticLogicInstruction {
    public JumpGreaterThanOrEqualTo(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 17 - Jump Greater Than or Equal To
     Octal: 021
     JGE r,x, address[,I]
     If c(r) >= 0, then PC <- EA
     Else PC <- PC + 1
     */
    public void execute() {
        System.out.println("JGE");
    }

    public void storeResult(){
        // NOOP
    }
}


class MultiplyRegisterByRegister extends ArithmeticLogicInstruction {
    public MultiplyRegisterByRegister(short word, Simulator context) {
        super(word, context);
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
    public void execute() {
        System.out.println("MLT");
    }

    public void storeResult(){
        // NOOP
    }
}

class DivideRegisterByRegister extends ArithmeticLogicInstruction {
    public DivideRegisterByRegister(short word, Simulator context) {
        super(word, context);
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
    public void execute() {
        System.out.println("DVD");
    }

    public void storeResult(){
        // NOOP
    }
}

class TestTheEqualityOfRegisterAndRegister extends ArithmeticLogicInstruction {
    public TestTheEqualityOfRegisterAndRegister(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 22 - Test the Equality of Register and Register
     Octal: 026
     TRR rx, ry
     If c(rx) = c(ry), set cc(4) <- 1; else, cc(4) <- 0
     */
    public void execute() {
        System.out.println("TRR");
    }

    public void storeResult(){
        // NOOP
    }
}

class LogicalAndOfRegisterAndRegister extends ArithmeticLogicInstruction {
    public LogicalAndOfRegisterAndRegister(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 23 - Logical And of Register and Register
     Octal: 027
     AND rx, ry
     c(rx) <- c(rx) AND c(ry)
     */
    public void execute() {
        System.out.println("AND");
    }

    public void storeResult(){
        // NOOP
    }
}

class LogicalOrOfRegisterAndRegister extends ArithmeticLogicInstruction {
    public LogicalOrOfRegisterAndRegister(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 24 - Logical Or of Register and Register
     Octal: 030
     ORR rx, ry
     c(rx) <- c(rx) OR c(ry)
     */
    public void execute() {
        System.out.println("ORR");
    }

    public void storeResult(){
        // NOOP
    }
}

class LogicalNotOfRegisterAndRegister extends ArithmeticLogicInstruction {
    public LogicalNotOfRegisterAndRegister(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 25 - Logical Not of Register To Register
     Octal: 031
     NOT rx
     C(rx) <- NOT c(rx)
     */
    public void execute() {
        System.out.println("NOT");
    }

    public void storeResult(){
        // NOOP
    }
}
