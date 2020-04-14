package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by MLT, DVD, TRR, AND, ORR, NOT
public class FloatingPointVectorInstruction extends Instruction {
    FloatingPointVectorInstruction(short word, Simulator context) {
        super(word, context);
        // TODO: Implement Parsing
    }

    public void print(){
        System.out.println("OpCode: " + this.opCode);
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

}
