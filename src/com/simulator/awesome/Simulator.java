package com.simulator.awesome;

import static com.simulator.awesome.Utils.*;

public class Simulator {

    // Control Unit, including the IR
    public ControlUnit cu;

    // Memory Subsystem, including the MAR, MBR, Cache, and Linear Memory
    public final Memory memory;

    // Program Counter: Address of the next instruction to be executed
    public ProgramCounter pc;

    // Internal Address Register. Used for moving data around in the CPU.
    private short iar;

    // Condition code: overflow, underflow, division by zero, equal-or-not.
    public ConditionCode cc;

    // Machine Fault Register: Illegal Access to Privileged memory, Illegal Trap, Illegal OpCode, Access beyond bounds
    public MachineFaultRegister mfr;

    public final MachineStatusRegister msr;

    // The four general purpose registers
    private short r0, r1, r2, r3;

    // The three index registers
    private short x1, x2, x3;

    // The two floating point registers
    private short fr0, fr1;

    // Arithmetic Logic Unit
    public ArithmeticLogicUnit alu;

    // Floating Point  Unit
    public FloatingPointUnit fpu;

    // I/O Subsystem
    public final InputOutput io;

    public final ReadOnlyMemory rom;

    Simulator(int wordCount) {
        this.cu = new ControlUnit(this);
        this.memory = new Memory(this, wordCount);
        this.alu = new ArithmeticLogicUnit(this);
        this.fpu = new FloatingPointUnit(this);
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
        this.cc.reset();
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
        this.fr0 = 0;
        this.fr1 = 1;
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

    public short getFloatingRegister(short registerId) {
        if(registerId == 0){
            return this.fr0;
        } else if (registerId == 1){
            return this.fr1;
        } else {
            throw new RuntimeException("Invalid Floating Point Register!");
        }
    }

    public void setFloatingRegister(short registerId, short value) {
        if(registerId == 0){
            this.fr0 = value;
        } else if (registerId == 1){
            this.fr1 = value;
        } else {
            throw new RuntimeException("Invalid Floating Point Register!");
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
                try {
                    this.memory.store((short)(baseAddress + i), words[i]);
                } catch (IllegalMemoryAccessToReservedLocationsException | IllegalMemoryAddressBeyondLimitException e) {
                    e.printStackTrace();
                }
            }
            return baseAddress;
        } else {
            short baseAddress = (alignLeft) ? (short) memoryPosition : (short)(memoryPosition - words.length);
            for (int i = 0; i < words.length; i++) {
                try {
                    this.memory.store((short)(baseAddress + i), words[i]);
                } catch (IllegalMemoryAccessToReservedLocationsException | IllegalMemoryAddressBeyondLimitException e) {
                    e.printStackTrace();
                }
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

    // Writes the program address as an indirect at address 7
    // Saving this location in program memory allows traps to reload the program later
    public void setAsUserProgram(short programAddress){
        boolean isSupervisor = this.msr.isSupervisorMode();
        this.msr.setSupervisorMode(true);
        try {
            this.memory.store((short)7, programAddress);
        } catch (IllegalMemoryAccessToReservedLocationsException | IllegalMemoryAddressBeyondLimitException e) {
            e.printStackTrace();
        }
        this.msr.setSupervisorMode(isSupervisor);
    }

    public void loadUserProgram(String[] assembledMachineCode, short programAddress){
        this.loadProgram(assembledMachineCode, programAddress, true, false);
        // The Heap Space starts one word after the user program. However, if the user loads multiple programs,
        // we want to place the heap space after the furthest in memory
        short baseHeapSpace = (short) (programAddress + assembledMachineCode.length + 1);
        if (this.memory.baseHeapSpace <  baseHeapSpace) this.memory.baseHeapSpace = baseHeapSpace;
        this.setAsUserProgram(programAddress);

        // How many 32-word chunks are available from baseHeapSpace to baseUpperReadOnlyMemory (exclusive)
        short heapSpaceSize = (short) (this.memory.baseUpperReadOnlyMemory - this.memory.baseHeapSpace);
        short datasetBodyChunks = (short) (heapSpaceSize / 32);

        System.out.println("Heap sees " + (this.memory.baseUpperReadOnlyMemory - this.memory.baseHeapSpace) + " words free.");
        // A dataset header can only hold 31 indirects.
        if (datasetBodyChunks > 31) datasetBodyChunks = 31;

        if (datasetBodyChunks > 0) {
            String zeroedOutData[] = new String[datasetBodyChunks*32];
            for (int i = 0; i < zeroedOutData.length; i++) zeroedOutData[i] = "0000000000000000";
            this.loadProgram(zeroedOutData, baseHeapSpace, true, true);

            try {
                this.memory.store((short) 18, baseHeapSpace);
                short addressOfDS = this.memory.fetch((short) 18);
                short sizeOfDS = this.memory.fetch((short) addressOfDS);
                System.out.println("Allocated a Heap Dataset with " + datasetBodyChunks + " sections, able to store " + sizeOfDS + " words");
            } catch (IllegalMemoryAccessToReservedLocationsException e) {
                e.printStackTrace();
            } catch (IllegalMemoryAddressBeyondLimitException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Insufficient space to allocate heap!");
        }
    }

    public short getCallStackFrameBase(short callStackDepth) {
        short baseAddress = 32;
        switch (callStackDepth){
            case 0:
                baseAddress = 32;
                break;
            case 1:
                baseAddress = 64;
                break;
            case 2:
                baseAddress = 96;
                break;
            case 3:
                baseAddress = 128;
                break;
        }
        return baseAddress;
    }

    public void incrementCallStack(short baseAddressOfCallee){
        boolean callerIsInSupervisor = this.msr.isSupervisorMode();

        short currentCallStackDepth = this.msr.getCallStackDepth();
        if ((currentCallStackDepth + 1) > 3) throw new Error("Stack Overflow!");
        if ((currentCallStackDepth + 1) < 0) throw new Error("Stack Underflow!");
        currentCallStackDepth++;
        this.msr.setCallStackDepth(currentCallStackDepth);

        // Update Address 16 to base address. If this is from userspace, we need to temporarily escalate to write
        if (!callerIsInSupervisor) this.msr.setSupervisorMode(true);
        short baseAddress = getCallStackFrameBase(currentCallStackDepth);
        try {
            this.memory.store((short)16, baseAddress);
        } catch (IllegalMemoryAccessToReservedLocationsException | IllegalMemoryAddressBeyondLimitException e) {
            e.printStackTrace();
        }
        if (!callerIsInSupervisor) this.msr.setSupervisorMode(false);

        // Write information to stack frame
        try {
            this.memory.store(baseAddress, baseAddressOfCallee);
        } catch (IllegalMemoryAccessToReservedLocationsException | IllegalMemoryAddressBeyondLimitException e) {
            e.printStackTrace();
        }
    }

    public void decrementCallStack(){
        boolean callerIsInSupervisor = this.msr.isSupervisorMode();

        short currentCallStackDepth = this.msr.getCallStackDepth();
        if ((currentCallStackDepth - 1) > 3) throw new Error("Stack Overflow!");
        if ((currentCallStackDepth - 1) < 0) throw new Error("Stack Underflow!");

        // Zero out old frame
        short currentCallStackBase = getCallStackFrameBase(currentCallStackDepth);
        short sizeOfCallStackFrame = 32;
        try {
            for (short i = 0; i < sizeOfCallStackFrame; i++) {
                this.memory.store((short)(currentCallStackBase + i),(short)0);
            }
        } catch (IllegalMemoryAccessToReservedLocationsException | IllegalMemoryAddressBeyondLimitException e) {
            e.printStackTrace();
        }

        short newCallStackDepth = (short)(currentCallStackDepth - 1);
        short newCallStackBase = getCallStackFrameBase(newCallStackDepth);

        // Update Address 16 to base address. If this is from userspace, we need to temporarily escalate to write
        if (!callerIsInSupervisor) this.msr.setSupervisorMode(true);
        try {
            this.memory.store((short)16,newCallStackBase);
        } catch (IllegalMemoryAccessToReservedLocationsException | IllegalMemoryAddressBeyondLimitException e) {
            e.printStackTrace();
        }
        if (!callerIsInSupervisor) this.msr.setSupervisorMode(false);

        this.msr.setCallStackDepth(newCallStackDepth);
    }

}