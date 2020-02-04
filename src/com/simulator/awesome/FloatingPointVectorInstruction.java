package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by MLT, DVD, TRR, AND, ORR, NOT
public class FloatingPointVectorInstruction extends Instruction {
    FloatingPointVectorInstruction(short word, Simulator context) {
        super(word, context);
        // TODO: Implement Parsing

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
    }
}

class FloatingAddMemoryToRegister extends FloatingPointVectorInstruction {
    public FloatingAddMemoryToRegister(short word, Simulator context) {
        super(word, context);
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
    public void execute() {
        System.out.println("FADD");
    }

    public void storeResult(){
        // NOOP
    }
}

class FloatingSubtractMemoryFromRegister extends FloatingPointVectorInstruction {
    public FloatingSubtractMemoryFromRegister(short word, Simulator context) {
        super(word, context);
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
    public void execute() {
        System.out.println("FSUB");
    }

    public void storeResult(){
        // NOOP
    }
}

class VectorAdd extends FloatingPointVectorInstruction {
    public VectorAdd(short word, Simulator context) {
        super(word, context);
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
    public void execute() {
        System.out.println("VADD");
    }

    public void storeResult(){
        // NOOP
    }
}

class VectorSubtract extends FloatingPointVectorInstruction {
    public VectorSubtract(short word, Simulator context) {
        super(word, context);
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
    public void execute() {
        System.out.println("VSUB");
    }

    public void storeResult(){
        // NOOP
    }
}

class ConvertToFixedOrFloatingPoint extends FloatingPointVectorInstruction {
    public ConvertToFixedOrFloatingPoint(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 37 - Convert to Fixed/FloatingPoint
     Octal: 045
     CNVRT r, x, address[,I]
     If F = 0, convert c(EA) to a fixed point number and store in r.
     If F = 1, convert c(EA) to a floating point number and store in FR0.
     The r register contains the value of F before the instruction is executed.
     */
    public void execute() {
        System.out.println("CNVRT");
    }

    public void storeResult(){
        // NOOP
    }
}

class LoadFloatingPointFromMemory extends FloatingPointVectorInstruction {
    public LoadFloatingPointFromMemory(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 50 - Load Floating Register From Memory
     Octal: 062
     LDFR fr, x, address [,i]
     fr = 0..1
     fr <- c(EA), c(EA+1)
     fr <- c(c(EA), c(EA)+1), if I bit set
     */
    public void execute() {
        System.out.println("LDFR");
    }

    public void storeResult(){
        // NOOP
    }
}

class StoreFloatingPointToMemory extends FloatingPointVectorInstruction {
    public StoreFloatingPointToMemory(short word, Simulator context) {
        super(word, context);
    }

    /**
     OPCODE 51 - Store Floating Register To Memory
     Octal: 063
     STFR fr, x, address [,i]
     fr = 0..1
     EA, EA+1 <- c(fr)
     c(EA), c(EA)+1 <- c(fr), if I-bit set
     */
    public void execute() {
        System.out.println("STFR");
    }

    public void storeResult(){
        // NOOP
    }
}
