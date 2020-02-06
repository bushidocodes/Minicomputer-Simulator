package com.simulator.awesome;

public class Simulator {

    // The number of 16-bit words we have in memory
    private int wordCount;

    // The linear memory of our simulated system.
    // Shorts in Java are 16-bit, so this is word addressable
    // The index represents the nth word, zero indexed
    private Short memory[];

    // Program Counter: Address of the next instruction to be executed
    // Uses the least significant 12 bits
    private short pc;
    static final short PC_MASK = (short)0b0000111111111111;

    // Condition code.
    // Uses the least significant 4 bits
    // set when arithmetic/logical operations are executed
    // it has four 1-bit elements: overflow, underflow, division by zero, equal-or-not.
    // ID   |   Fault
    // 0001 |   Overflow
    // 0010 |   Underflow
    // 0100 |   Division by Zero
    // 1000 |   Equal Or Not
    private byte cc;

    // Instruction register. Holds the instruction to be executed
    private short ir;

    // Internal Address Register. Used for moving data around in the CPU.
    private short iar;

    // Memory Address Register. Holds the address of the word to be fetched from memory
    private short mar;
    static final short MAR_MASK = (short)0b0000111111111111;

    // Memory Buffer Register.
    // Holds the word just fetched or the word to be stored into memory.
    // If this is a word we're writing to memory, it can either be something to be written
    // or something that we've already written, ya dig?
    private short mbr;

    // Machine Fault Register.
    // Contains the ID code of a machine fault after it occurs
    // DO NOT IMPLEMENT UNTIL PHASE 3
    // ID   |   Fault
    // 0001 |   Illegal Memory Address to Reserved Locations
    // 0010 |   Illegal TRAP code
    // 0100 |   Illegal Operation Code
    // 1000 |   Illegal Memory Address beyond 2048 (or higher is memory is expanded)
    private byte mfr;

    // The four general purpose registers
    private short r0, r1, r2, r3;

    // The three index registers
    private short x1, x2, x3;

    /** The execution step, 1-6
     * 1. Instruction Fetch
     * 2. Instruction Decode
     * 3. Operand Fetch
     * 4. Execute
     * 5. Result Store
     * 6. Next Instruction
     **/
    private int executionStep = 1;

    // Loop state
    private boolean isRunning = false;
    private short currentOpcode;
    private Instruction currentInstruction;

    // TODO: What are the mystery registers we're missing?

    Simulator(int wordCount) {
        // Allocate Linear Memory
        this.wordCount = 2048;
        this.memory = new Short[wordCount];

        for (int i = 0; i < wordCount; i++) {
            this.memory[i] = 0;
        }

        // Allocate and zero out all Registers
        this.pc = 0;
        this.cc = 0;
        this.ir = 0;
        this.mar = 0;
        this.mbr = 0;
        this.mfr = 0;
        this.r0 = 0;
        this.r1 = 0;
        this.r2 = 0;
        this.r3 = 0;
        this.x1 = 0;
        this.x2 = 0;
        this.x3 = 0;
    }

    public void reset(){
        // Allocate Linear Memory
        this.wordCount = 2048;
        this.memory = new Short[wordCount];

        for (int i = 0; i < wordCount; i++) {
            this.memory[i] = 0;
        }

        // Allocate and zero out all Registers
        this.pc = 0;
        this.cc = 0;
        this.ir = 0;
        this.mar = 0;
        this.mbr = 0;
        this.mfr = 0;
        this.r0 = 0;
        this.r1 = 0;
        this.r2 = 0;
        this.r3 = 0;
        this.x1 = 0;
        this.x2 = 0;
        this.x3 = 0;
    }

    public short getWord(int address) {
        if (address < 6) {
            // Illegally accessing protected memory
            // In the future, we'll set MFR to xxx1, but for now, we can just halt
            System.out.println("getWord - Illegally accessing protected address " + address + "! Halting");
            System.exit(1);
        } else if (address > this.wordCount) {
            // Illegally accessing protecting memory above limit
            // In the future, we'll set MFT to 1xxx, but for now, we can just halt
            System.out.println("Illegally accessing address above highest memory address. Halting!");
            System.exit(1);
        }

        return this.memory[address];
    }

    public void setWord(int address, short value){
        if (address < 6) {
            // Illegally accessing protected memory
            // In the future, we'll set MFR to xxx1, but for now, we can just halt
            System.out.println("setWord - Illegally accessing protecting address! Halting");
            System.exit(1);
        } else if (address > this.wordCount) {
            // Illegally accessing protecting memory above limit
            // In the future, we'll set MFT to 1xxx, but for now, we can just halt
            System.out.println("Illegally accessing address above highest memory address. Halting!");
            System.exit(1);
        } else {
            try {
                this.memory[address] = value;

            } catch (Exception err) {
                System.err.println("Accessing " + address + " causes " + err);
            }
        }
    }

    public short getProgramCounter() {
        return (short) (this.pc & Simulator.PC_MASK);
    }

    public void setProgramCounter(short unmaskedPC) {
        this.pc = (short) (unmaskedPC & Simulator.PC_MASK);
    }

    /**
     * A Helper getter for bit arrays
     * @param bitArray - The integral value that we are treating as an array of bits
     * @param offset - The offset from the least significant bit. 0 is b0001
     * @return - True if bit is set
     */
    private static boolean getNthLeastSignificantBit(int bitArray, int offset) {
        int getterMask = (0b00000001<<offset);
        return (bitArray & getterMask) == getterMask;
    }

    /**
     * A Helper setting for bit arrays
     * @param bitArray - The integral that we are treating as an array of bits
     * @param offset - The offset from the least significant bit. 0 is b0001
     * @param isSet - The resulting bit array after manipulating the bit
     */
    private static int setNthLeastSignificantBit(int bitArray, int offset, boolean isSet) {
        int setterMask = (0b00000001<<offset);
        return isSet ? (bitArray | setterMask) : (bitArray & ~setterMask);
    }

    public boolean isOverflow() {
        return getNthLeastSignificantBit(this.cc, 0);
    }

    public void setOverflow(boolean isOverflow) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 0, isOverflow);
    }

    public boolean isUnderflow() {
        return getNthLeastSignificantBit(this.cc, 1);
    }

    public void setUnderflow(boolean isUnderflow) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 1, isUnderflow);
    }

    public boolean isDivideByZero() {
        return getNthLeastSignificantBit(this.cc, 2);
    }

    public void setDivideByZero(boolean isDivideByZero) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 2, isDivideByZero);
    }

    public boolean isEqualOrNot() {
        return getNthLeastSignificantBit(this.cc, 3);
    }

    public void setEqualOrNot(boolean isEqualOrNot) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 3, isEqualOrNot);
    }

    public short getInternalAddressRegister() {
        return this.iar;
    }

    public void setInternalAddressRegister(short value) {
        this.iar = value;
    }

    public short getMemoryAddressRegister() {
        return (short) (this.mar & Simulator.MAR_MASK);
    }

    public void fetchMemoryAddressRegister() {
        this.mbr = this.getWord(this.mar);
    }

    public void setMemoryAddressRegister(short unmaskedMAR) {
        this.mar = (short) (unmaskedMAR & Simulator.PC_MASK);
    }

    public short getMemoryBufferRegister() {
        return this.mbr;
    }

    public void setMemoryBufferRegister(short mbr) {
        this.mbr = mbr;
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

    public boolean isIllegalMemoryAccessToReservedLocations() {
        return getNthLeastSignificantBit(this.mfr, 0);
    }

    public void setIllegalMemoryAccessToReservedLocations(boolean isIllegalMemoryAccessToReservedLocations) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 0, isIllegalMemoryAccessToReservedLocations);
    }

    public boolean isIllegalTrapCode() {
        return getNthLeastSignificantBit(this.mfr, 1);
    }

    public void setIllegalTrapCode(boolean isIllegalTrapCode) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 1, isIllegalTrapCode);
    }

    public boolean isIllegalOpcode() {
        return getNthLeastSignificantBit(this.mfr, 2);
    }

    public void setIsIllegalOpcode(boolean isIllegalOpcode) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 2, isIllegalOpcode);
    }

    public boolean isIllegalMemoryAddressBeyondLimit() {
        return getNthLeastSignificantBit(this.mfr, 3);
    }

    public void setIsIllegalMemoryAddressBeyondLimit(boolean isIllegalMemoryAddressBeyondLimit) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 3, isIllegalMemoryAddressBeyondLimit);
    }

    public boolean isRunning(){
        return this.isRunning;
    }

    public void setIsRunning(boolean running){
        this.isRunning = running;
    }

    /**
     *
     * @param word - the machine word you want to extract the OPCODE from
     * @return - The value of the opcode from the word
     */
    private static short extractOpCode(short word) {
        // OPCODE is 0-5bits, so right shift by 10
        // the >>> operator is a bitshift that includes the "sign bit"
        short opcode = (short)((word>>>10) & 0b0000000000111111);
        return opcode;
    }

    public void execute() {
        this.currentInstruction.execute();
    }

    /**
     * Given a 16-bit short, generates a binary string
     * @param word
     * @return String of 1s and 0s showing binary layout of memory word
     */
    public static String wordToString(short word){
        String binaryString = Integer.toBinaryString(Short.toUnsignedInt(word));
        return String.format("%1$16s", binaryString).replace(' ', '0');
    }

    public void loadProgram(String[] assembledMachineCode, int memoryLocation){
        // Iterate through memory line-by-line, loading in the corresponding machine code from the program.
        int computerMemoryLoc = memoryLocation;
        for (int inputMemoryLoc = 0; inputMemoryLoc<assembledMachineCode.length; inputMemoryLoc++){
            this.setWord(computerMemoryLoc, (short)Integer.parseUnsignedInt(assembledMachineCode[inputMemoryLoc],2));
            computerMemoryLoc++;
        }
    }

    // This function allows you to not specify a memory location for loading a program (default to location 6)
    public void loadProgram(String[] assembledMachineCode){
        // No memory location was specified, load the program into memory starting at the first location 6
        loadProgram(assembledMachineCode, 6);
    }

    public void dumpRegistersToJavaConsole(){
        System.out.println("=======================================");
        System.out.println("Registers");
        System.out.println("=======================================");
        System.out.println("Program Counter: " + Simulator.wordToString(this.pc));
        System.out.println("Condition Code: " + Simulator.wordToString(this.cc));
        System.out.println("Instruction Register: " + Simulator.wordToString(this.ir));
        System.out.println("Internal Address Register: " + Simulator.wordToString(this.iar));
        System.out.println("Memory Address Register: " + Simulator.wordToString(this.mar));
        System.out.println("Memory Buffer Register: " + Simulator.wordToString(this.mbr));
        System.out.println("Memory Fault Register: " + Simulator.wordToString(this.mfr));
        System.out.println("General Register 0: " + Simulator.wordToString(this.r0));
        System.out.println("General Register 1: " + Simulator.wordToString(this.r1));
        System.out.println("General Register 2: " + Simulator.wordToString(this.r2));
        System.out.println("General Register 3: " + Simulator.wordToString(this.r3));
        System.out.println("Index Register 1: " + Simulator.wordToString(this.x1));
        System.out.println("Index Register 2: " + Simulator.wordToString(this.x2));
        System.out.println("Index Register 3: " + Simulator.wordToString(this.x3));
        System.out.println("Current OPCODE: " + Simulator.wordToString(this.currentOpcode));
        System.out.println("=======================================");
    }

    public void dumpMemoryToJavaConsole(){
        System.out.println("=======================================");
        System.out.println("Memory Dump (Excluding zeroed out words");
        System.out.println("=======================================");
        for (int i=0; i<this.wordCount; i++) {
            try{
                if (memory[i] != 0) System.out.printf("Address: %4d: %s\n",  i, Simulator.wordToString(memory[i]));
            } catch(NullPointerException e) {
                //null value in memory-- carry on
            }
        }
        System.out.println("=======================================");
    }

    /** The execution step, 1-6
     * 1. Instruction Fetch
     * 2. Instruction Decode
     * 3. Operand Fetch
     * 4. Execute
     * 5. Result Store
     * 6. Next Instruction
     **/

    public void singleStep(){
        switch (this.executionStep){
            // Instruction Fetch
            case 1:
                this.executionInstructionFetch();
                break;
            // Instruction Decode
            case 2:
                this.executionInstructionDecode();
                break;
            // Operand Fetch
            case 3:
                this.currentInstruction.fetchOperand();
                break;
            // Execute
            case 4:
                this.currentInstruction.execute();
                break;
            // Result Store
            case 5:
                /**
                 In 1 cycle, move the contents of the IRR to:
                 a. If target = register, use Register Select 1 to store IRR contents into the specified register
                 b. If target = memory, such as a STR, move contents of IRR to MBR. On the next cycle, move contents of MBR to memory using address in MAR.
                 **/
                this.currentInstruction.storeResult();
                break;
            // Next Instruction
            case 6:
                this.executionNextInstruction();
                break;
        }
        if (this.executionStep == 6){
            this.dumpRegistersToJavaConsole();
            this.dumpMemoryToJavaConsole();
            this.executionStep = 1;
        } else {
            this.executionStep++;
        }
    }

    // Execution Step 1
    // Obtain Instruction from Program Storage
    private void executionInstructionFetch() {
        System.out.println("PC: " + this.pc);
        // MAR <- PC
        // Transfer Program Counter to Memory Address Register
        this.mar = this.pc;

        // MBR <-MEM[MAR]
        // Fetch the word located at the MAR location from memory and transfer it to Memory Buffer Register.
        this.fetchMemoryAddressRegister();
    }

    // Execution Step 2
    // Determine Operation Required
    private void executionInstructionDecode() {
        // IR <- MBR
        // Transfer Memory Buffer Register to Instruction Register
        this.ir = this.mbr;

        // Extract the opcode from the IR and use to set currentInstruction
        switch(Simulator.extractOpCode(this.ir)) {
            case 0:
                this.currentInstruction = new Halt(this.ir, this);
                break;
            case 1:
                this.currentInstruction = new LoadRegisterFromMemory(this.ir, this);
                break;
            case 2:
                this.currentInstruction = new StoreRegisterToMemory(this.ir, this);
                break;
            case 3:
                this.currentInstruction = new LoadRegisterWithAddress(this.ir, this);
                break;
            case 4:
                this.currentInstruction = new AddMemoryToRegister(this.ir, this);
                break;
            case 5:
                this.currentInstruction = new SubtractMemoryFromRegister(this.ir, this);
                break;
            case 6:
                this.currentInstruction = new AddImmediateToRegister(this.ir, this);
                break;
            case 7:
                this.currentInstruction = new SubtractImmediateFromRegister(this.ir, this);
                break;
            case 10:
                this.currentInstruction = new JumpIfZero(this.ir, this);
                break;
            case 11:
                this.currentInstruction = new JumpIfNotEqual(this.ir, this);
                break;
            case 12:
                this.currentInstruction = new JumpIfConditionCode(this.ir, this);
                break;
            case 13:
                this.currentInstruction = new UnconditionalJumpToAddress(this.ir, this);
                break;
            case 14:
                this.currentInstruction = new JumpAndSaveReturnAddress(this.ir, this);
                break;
            case 15:
                this.currentInstruction = new ReturnFromSubroutine(this.ir, this);
                break;
            case 16:
                this.currentInstruction = new SubtractOneAndBranch(this.ir, this);
                break;
            case 17:
                this.currentInstruction = new JumpGreaterThanOrEqualTo(this.ir, this);
                break;
            case 20:
                this.currentInstruction = new MultiplyRegisterByRegister(this.ir, this);
                break;
            case 21:
                this.currentInstruction = new DivideRegisterByRegister(this.ir, this);
                break;
            case 22:
                this.currentInstruction = new TestTheEqualityOfRegisterAndRegister(this.ir, this);
                break;
            case 23:
                this.currentInstruction = new LogicalAndOfRegisterAndRegister(this.ir, this);
                break;
            case 24:
                this.currentInstruction = new LogicalOrOfRegisterAndRegister(this.ir, this);
                break;
            case 25:
                this.currentInstruction = new LogicalNotOfRegisterAndRegister(this.ir, this);
                break;
            case 30:
                this.currentInstruction = new Trap(this.ir, this);
                break;
            case 31:
                this.currentInstruction = new ShiftRegisterByCount(this.ir, this);
                break;
            case 32:
                this.currentInstruction = new RotateRegisterByCount(this.ir, this);
                break;
            case 33:
                this.currentInstruction = new FloatingAddMemoryToRegister(this.ir, this);
                break;
            case 34:
                this.currentInstruction = new FloatingSubtractMemoryFromRegister(this.ir, this);
                break;
            case 35:
                this.currentInstruction = new VectorAdd(this.ir, this);
                break;
            case 36:
                this.currentInstruction = new VectorSubtract(this.ir, this);
                break;
            case 37:
                this.currentInstruction = new ConvertToFixedOrFloatingPoint(this.ir, this);
                break;
            case 41:
                this.currentInstruction = new LoadIndexRegisterFromMemory(this.ir, this);
                break;
            case 42:
                this.currentInstruction = new StoreIndexRegisterToMemory(this.ir, this);
                break;
            case 50:
                this.currentInstruction = new LoadFloatingPointFromMemory(this.ir, this);
                break;
            case 51:
                this.currentInstruction = new StoreFloatingPointToMemory(this.ir, this);
                break;
            case 61:
                this.currentInstruction = new InputCharacterToRegisterFromDevice(this.ir, this);
                break;
            case 62:
                this.currentInstruction = new OutputCharacterToDeviceFromRegister(this.ir, this);
                break;
            case 63:
                this.currentInstruction = new CheckDeviceStatusToRegister(this.ir, this);
                break;
        }
    }

    // Execution Step 6
    // Determine Next Instruction
    private void executionNextInstruction() {
        // TODO: This will need to be expanded when we implement branching
        // Increment the Program Counter
        if (this.pc<this.wordCount-1){
            this.pc++;
        } else {
            pauseExecutionLoop();
        }
    }

    // Allow setting the program counter when booting from the command line.
    public void powerOn(short programCounter){
        // Check if program counter was set within the allowable range.
        if(programCounter>5 && programCounter<wordCount){
            setProgramCounter(programCounter);
        } else {
            setProgramCounter((short) 6);
        }
    }

    // If program counter was not specified, default to 6.
    public void powerOn(){
        powerOn((short) 6);
    }

    public void startExecutionLoop(){
        this.setIsRunning(true);
        while(this.isRunning){
            singleStep();
        }
    }

    public void pauseExecutionLoop(){
        this.setIsRunning(false);
    }
}