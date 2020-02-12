package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by LDR, STR, LDA, LDX, STX

enum ShiftRotateType {
    LOGICAL, ARITHMETIC
}

enum ShiftRotateDirection {
    LEFT, RIGHT
}

public class ShiftRotateInstruction extends Instruction{
    public short registerId;
    public ShiftRotateType type; // If not logical, is arithmetic
    public ShiftRotateDirection direction;
    public short count;
    protected short buffer;

    ShiftRotateInstruction(short word, Simulator context) {
        super(word, context);
        short registerMask           = (short) 0b0000001100000000;
        short logicalArithmeticMask  = (short) 0b0000000010000000;
        short leftRightMask          = (short) 0b0000000001000000;
        short countMask              = (short) 0b0000000000001111;
        // This is effectively an implicit register, but these operations might be atomic and executed in place


        short countOffset            = 0;
        short registerOffset         = 8;

        this.count = Utils.short_unsigned_right_shift((short)(word & countMask), countOffset );
        this.type = (word & logicalArithmeticMask) == logicalArithmeticMask ? ShiftRotateType.LOGICAL : ShiftRotateType.ARITHMETIC;
        this.direction = (word & leftRightMask) == leftRightMask ? ShiftRotateDirection.LEFT : ShiftRotateDirection.RIGHT;
        this.registerId = Utils.short_unsigned_right_shift((short)(word & registerMask), registerOffset );
    }

    public void fetchOperand(){
        this.buffer = this.context.getGeneralRegister(this.registerId);
    }

    public void execute(){
        // NOOP
    }

    public void storeResult(){
        this.context.setGeneralRegister(this.registerId, this.buffer);
    }

    public void print(){
        System.out.println("OpCode: " + Short.toUnsignedInt(this.opCode));
        System.out.println("Register ID: " + this.registerId);
        System.out.println("Type: " + this.type);
        System.out.println("Direction : " + this.direction);
        System.out.println("Count: " + this.count);
    }
}

/**
 OPCODE 31 - Shift Register by Count
 Octal: 037
 SRC r, count, L/R, A/L
 c(r) is shifted left (L/R =1) or right (L/R = 0) either logically (A/L = 1) or arithmetically (A/L = 0)
 XX, XXX are ignored
 Count = 0…15
 If Count = 0, no shift occurs
 */
class ShiftRegisterByCount extends ShiftRotateInstruction {
    public ShiftRegisterByCount(short word, Simulator context) {
        super(word, context);
    }

    public void execute(){
        if (this.type == ShiftRotateType.ARITHMETIC) {
            if (this.direction == ShiftRotateDirection.LEFT) {
                this.buffer <<= this.count;
            } else if (this.direction == ShiftRotateDirection.RIGHT) {
                this.buffer >>= this.count;
            }
        } else if (this.type == ShiftRotateType.LOGICAL) {
            if (this.direction == ShiftRotateDirection.LEFT) {
                // Note: Logical and arithmetic left shifts operate identically.
                // https://www.quora.com/Why-is-there-no-unsigned-left-shift-operator-in-Java
                this.buffer <<= this.count;
            } else if (this.direction == ShiftRotateDirection.RIGHT) {
                this.buffer = Utils.short_unsigned_right_shift(this.buffer, this.count );
            }
        }
    }

}

/**
 OPCODE 32 - Rotate Register by Count
 Octal: 040
 RRC r, count, L/R, A/L
 c(r) is rotated left (L/R = 1) or right (L/R =0) either logically (A/L =1)
 XX, XXX is ignored
 Count = 0…15
 If Count = 0, no rotate occurs
 */
class RotateRegisterByCount extends ShiftRotateInstruction {
    public RotateRegisterByCount(short word, Simulator context) {
        super(word, context);
    }

    public void execute(){
        // Note: There is no such thing as a logical versus arithmetic rotate. I suspect this is an error in the spec, so ignore
        if (this.direction == ShiftRotateDirection.LEFT) {
            this.buffer = (short)((this.buffer << this.count) | Utils.short_unsigned_right_shift(this.buffer, (Short.SIZE - this.count)));
        } else if (this.direction == ShiftRotateDirection.RIGHT) {
            this.buffer = (short)(Utils.short_unsigned_right_shift(this.buffer, this.count) | (this.buffer << (Short.SIZE - this.count)));
        }
    }

}
