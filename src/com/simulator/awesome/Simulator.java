package com.simulator.awesome;

import static com.simulator.awesome.Utils.*;

public class Simulator {

    // Control Unit, including the IR
    public ControlUnit cu;

    // Memory Subsystem, including the MAR, MBR, Cache, and Linear Memory
    public Memory memory;

    // Program Counter: Address of the next instruction to be executed
    public ProgramCounter pc;

    // Internal Address Register. Used for moving data around in the CPU.
    private short iar;

    // Condition code: overflow, underflow, division by zero, equal-or-not.
    public ConditionCode cc;

    // Machine Fault Register: Illegal Access to Privileged memory, Illegal Trap, Illegal OpCode, Access beyond bounds
    public MachineFaultRegister mfr;

    public MachineStatusRegister msr;

    // The four general purpose registers
    private short r0, r1, r2, r3;

    // The three index registers
    private short x1, x2, x3;

    // Arithmetic Logic Unit
    public ArithmeticLogicUnit alu;

    // I/O Subsystem
    public InputOutput io;

    public ReadOnlyMemory rom;

    Simulator(int wordCount) {
        this.cu = new ControlUnit(this);
        this.memory = new Memory(this, wordCount);
        this.alu = new ArithmeticLogicUnit(this);
        this.io = new InputOutput(this);
        this.pc = new ProgramCounter();
        this.cc = new ConditionCode();
        this.msr = new MachineStatusRegister();
        this.mfr = new MachineFaultRegister();
        this.rom = new ReadOnlyMemory(this);
        this.iar = 0;
        this.r0 = 0;
        this.r1 = 0;
        this.r2 = 0;
        this.r3 = 0;
        this.x1 = 0;
        this.x2 = 0;
        this.x3 = 0;
    }

    public void attachConsole(){
        this.msr.setIsInteractive(true);
    }

    public void detachConsole(){
        this.msr.setIsInteractive(false);
    }

    public void reset(){
        this.cu = new ControlUnit(this);
        this.memory.reset();
        this.alu = new ArithmeticLogicUnit(this);
        this.pc = new ProgramCounter();
        this.cc = new ConditionCode();
        this.msr.reset();
        this.mfr = new MachineFaultRegister();
        // No need to re-instantiate ROM
        this.io.reset();
        this.iar = 0;
        this.r0 = 0;
        this.r1 = 0;
        this.r2 = 0;
        this.r3 = 0;
        this.x1 = 0;
        this.x2 = 0;
        this.x3 = 0;
    }

    public short getInternalAddressRegister() {
        return this.iar;
    }

    public void setInternalAddressRegister(short value) {
        this.iar = value;
    }

    public short getGeneralRegister(short registerId) {
        if(registerId == 0){
            return this.r0;
        } else if (registerId == 1){
            return this.r1;
        } else if (registerId == 2){
            return this.r2;
        } else if (registerId == 3){
            return this.r3;
        } else {
            throw new RuntimeException("Invalid General Purpose Register!");
        }
    }

    public void setGeneralRegister(short registerId, short value) {
        if(registerId == 0){
            this.r0 = value;
        } else if (registerId == 1){
            this.r1 = value;
        } else if (registerId == 2){
            this.r2 = value;
        } else if (registerId == 3){
            this.r3 = value;
        } else {
            throw new RuntimeException("Invalid General Purpose Register!");
        }
    }

    /**
     * Get the value of an Index Register
     * @param registerId the index register to fetch [0,1,2,3]. 0 always resolves to 0
     * @return value of the Index Register or 0 if 0 was provided
     */
    public short getIndexRegister(short registerId) {
        if(registerId == 0){
            return 0;
        } else if(registerId == 1){
            return this.x1;
        } else if (registerId == 2){
            return this.x2;
        } else if (registerId == 3){
            return this.x3;
        } else {
            throw new RuntimeException("Invalid Index Register!");
        }
    }

    public void setIndexRegister(short registerId, short value) {
        if(registerId == 1){
            this.x1 = value;
        } else if (registerId == 2){
            this.x2 = value;
        } else if (registerId == 3){
            this.x3 = value;
        } else {
            throw new RuntimeException("Invalid Index Register!");
        }
    }

    // If memory location is a negative, aligns end address of program from address offset from number of words
    public short loadProgram(String[] assembledMachineCode, int memoryPosition, boolean alignLeft, boolean wrapInDataSet){
        // Convert machine code from strings to shorts
        short[] words = new short[assembledMachineCode.length];
        for (int i = 0; i < assembledMachineCode.length; i++){
            words[i] =  stringToWord(assembledMachineCode[i]);
        }

        if (wrapInDataSet) {
            DataSet ds = new DataSet(words);
            short baseAddress = alignLeft ? (short) memoryPosition : (short)(memoryPosition - ds.export().length);
            ds.setBaseAddress(baseAddress);
            words = ds.export();
            for (int i = 0; i < words.length; i++) {
                this.memory.store((short)(baseAddress + i), words[i]);
            }
            return baseAddress;
        } else {
            short baseAddress = (alignLeft) ? (short) memoryPosition : (short)(memoryPosition - words.length);
            for (int i = 0; i < words.length; i++) {
                this.memory.store((short)(baseAddress + i), words[i]);
            }
            return baseAddress;
        }
    }

    public void dumpRegistersToJavaConsole(){
        this.io.engineerConsolePrintLn("===============================");
        this.io.engineerConsolePrintLn("Registers");
        this.io.engineerConsolePrintLn("===============================");
        this.io.engineerConsolePrintLn("Program Counter: " + this.pc.toString());
        this.io.engineerConsolePrintLn("Condition Code: " + this.cc.toString());
        this.io.engineerConsolePrintLn("Instruction Register: " + wordToString(this.cu.getInstructionRegister()));
        this.io.engineerConsolePrintLn("Internal Address Register: " + wordToString(this.iar));
        this.io.engineerConsolePrintLn("Memory Address Register: " + this.memory.mar.toString());
        this.io.engineerConsolePrintLn("Memory Buffer Register: " + wordToString(this.memory.getMemoryBufferRegister()));
        this.io.engineerConsolePrintLn("Memory Fault Register: " + this.mfr.toString());
        this.io.engineerConsolePrintLn("General Register 0: " + wordToString(this.r0));
        this.io.engineerConsolePrintLn("General Register 1: " + wordToString(this.r1));
        this.io.engineerConsolePrintLn("General Register 2: " + wordToString(this.r2));
        this.io.engineerConsolePrintLn("General Register 3: " + wordToString(this.r3));
        this.io.engineerConsolePrintLn("Index Register 1: " + wordToString(this.x1));
        this.io.engineerConsolePrintLn("Index Register 2: " + wordToString(this.x2));
        this.io.engineerConsolePrintLn("Index Register 3: " + wordToString(this.x3));
        this.io.engineerConsolePrintLn("===============================");
    }

    // Allow setting the program counter when booting from the command line.
    public void powerOn(short programCounter){
        // Check if program counter was set within the allowable range.
        if(programCounter > 5 && programCounter < this.memory.getWordCount()){
            this.pc.set(programCounter);
        } else {
            this.pc.set((short) 6);
        }
    }

    // If program counter was not specified, default to 6.
    public void powerOn(){
        powerOn((short) 6);
    }
}