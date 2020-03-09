package com.simulator.awesome;

public class Instruction {
    public final short opCode;
    public final short word;
    protected final Simulator context;
    protected boolean didFault = false;

    public Instruction(short word, Simulator context) {
        short opCodeMask                = (short) 0b1111110000000000;
        short opCodeOffset              = 10;
        this.word = word;
        this.context = context;
        this.opCode = Utils.short_unsigned_right_shift((short)(word & opCodeMask), opCodeOffset);
    }

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // NOOP
    }

    public void execute(){
        // NOOP
    }

    public void storeResult() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // NOOP
    }

    public void validateGeneralRegisterIndex(short index){
        if (index < 0 || index > 3) {
            this.didFault = true;
        }
    }

    public void validateIndexRegisterIndex(short index){
        if (index < 0 || index > 3) {
            this.didFault = true;
        }
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
    final short trapCode;

    Trap(short word, Simulator context) {
        super(word, context);
        short trapCodeMask           = (short) 0b0000000000001111;
        short trapCodeOffset         = 0;
        this.trapCode = Utils.short_unsigned_right_shift((short)(word & trapCodeMask), trapCodeOffset);
    }

    public void fetchOperand() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
        // Switch to supervisor mode
        this.context.msr.setSupervisorMode(true);
        // Store PC (it is already incremented) to address 2
        this.context.memory.store((short) 2,this.context.pc.get());
        // Get the address of the trap handler table and offset to get the address of the correct trap
        short addressOfTrap = this.context.memory.fetch((short)(this.context.memory.fetch((short)0) + this.trapCode));
        // Jump to the trap
        this.context.pc.set(addressOfTrap);
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
        this.context.msr.setIsRunning(false);
        if (!this.context.msr.isInteractive()) {
            System.exit(1);
        }
    }

}