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

class FloatingAddMemoryToRegister extends FloatingPointVectorInstruction {
    public FloatingAddMemoryToRegister(short word, Simulator context) {
        super(word, context);
    }

}

class FloatingSubtractMemoryFromRegister extends FloatingPointVectorInstruction {
    public FloatingSubtractMemoryFromRegister(short word, Simulator context) {
        super(word, context);
    }

}

class VectorAdd extends FloatingPointVectorInstruction {
    public VectorAdd(short word, Simulator context) {
        super(word, context);
    }

}

class VectorSubtract extends FloatingPointVectorInstruction {
    public VectorSubtract(short word, Simulator context) {
        super(word, context);
    }

}

class ConvertToFixedOrFloatingPoint extends FloatingPointVectorInstruction {
    public ConvertToFixedOrFloatingPoint(short word, Simulator context) {
        super(word, context);
    }

}

class LoadFloatingPointFromMemory extends FloatingPointVectorInstruction {
    public LoadFloatingPointFromMemory(short word, Simulator context) {
        super(word, context);
    }

}

class StoreFloatingPointToMemory extends FloatingPointVectorInstruction {
    public StoreFloatingPointToMemory(short word, Simulator context) {
        super(word, context);
    }

}
