package com.simulator.awesome;

public class Instruction {
    public short opCode;
    public short word;
    protected Simulator context;
    protected boolean didFault = false;

    public Instruction(short word, Simulator context) {
        short opCodeMask                = (short) 0b1111110000000000;
        short opCodeOffset              = 10;
        this.word = word;
        this.context = context;
        this.opCode = Utils.short_unsigned_right_shift((short)(word & opCodeMask), opCodeOffset);
    }

    public void fetchOperand(){
        // NOOP
    }

    public void execute(){
        // NOOP
    }

    public void storeResult(){
        // NOOP
    }

    public void validateGeneralRegisterIndex(short index){
        if (index < 0 || index > 3) {
            this.didFault = true;
        };
    }

    public void validateIndexRegisterIndex(short index){
        if (index < 0 || index > 3) {
            this.didFault = true;
        };
    }

    public void print(){
        System.out.println("OpCode: " + this.opCode);
    }
}

/**
 * OPCODE 30 - Trap
 * Octal: 036
 * Traps to memory address 0, which contains the address of a table in memory.
 * Stores the PC+1 in memory location 2.
 * The table can have a maximum of 16 entries representing 16 routines for user-specified instructions stored elsewhere in memory.
 * Trap code contains an index into the table, e.g. it takes values 0 – 15.
 * When a TRAP instruction is executed, it goes to the routine whose address is in memory location 0,
 * executes those instructions, and returns to the instruction stored in memory location 2.
 * The PC+1 of the TRAP instruction is stored in memory location 2.
 */
class Trap extends Instruction {

    Trap(short word, Simulator context) {
        super(word, context);

        // TODO: Parse Trap Code
    }

    public void execute() {
        System.out.println("TRAP");
    }

    public void storeResult(){
        // NOOP
    }
}

/**
 OPCODE 00 - Halt the Machine
 Octal: 000
 HLT
 */
class Halt extends Instruction {

    Halt(short word, Simulator context) {
        super(word, context);
    }

    public void execute() {
        System.out.println("Halting...");
        this.context.setIsRunning(false);
        if (!this.context.isInteractive) {
            System.exit(1);
        }
    }

    public void storeResult(){
        // NOOP
    }
}