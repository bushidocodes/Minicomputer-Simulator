package com.simulator.awesome;

public class ControlUnit {
    Simulator context;

    // Instruction register. Holds the instruction to be executed
    // In certain architectures, also known as the current instruction register (CIR)
    private short ir;
    private Instruction currentInstruction;

    /** The execution step, 1-5
     * 1. Instruction Fetch
     * 2. Instruction Decode
     * 3. Operand Fetch
     * 4. Execute
     * 5. Result Store
     **/
    private int executionStep;

    ControlUnit(Simulator context) {
        this.context = context;
        this.ir = 0;
        this.executionStep = 1;
    }

    /**
     *
     * @param word - the machine word you want to extract the OPCODE from
     * @return - The value of the opcode from the word
     */
    private static short extractOpCode(short word) {
        // OPCODE is 0-5bits, so right shift by 10
        return Utils.short_unsigned_right_shift(word, 10);
    }

    public short getInstructionRegister() {
        return this.ir;
    }

    public void setInstructionRegister(short instructionRegister) {
        this.ir = instructionRegister;
    }

    /** The execution step, 1-5
     * 1. Instruction Fetch
     * 2. Instruction Decode
     * 3. Operand Fetch
     * 4. Execute
     * 5. Result Store
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
        }
        if (this.executionStep == 5){
            // Commenting out because this causing the program to hang
            // this.dumpRegistersToJavaConsole();
            // this.memory.dump();
            this.executionStep = 1;
        } else {
            this.executionStep++;
        }
    }

    // Execution Step 1
    // Obtain Instruction from Program Storage
    private void executionInstructionFetch() {
        this.context.io.engineerConsolePrintLn("PC: " + this.context.pc);
        if (this.context.pc.get() < 0 || this.context.pc.get() >= this.context.memory.getWordCount()){
            // TODO: Refactor as machine fault
            pauseExecutionLoop();
        }

        // MAR <- PC
        // MBR <- c[MAR]
        // IR <- MBR
        this.ir = this.context.memory.fetch(this.context.pc.get());

        // PC++
        this.context.pc.increment();
    }

    // Execution Step 2
    // Determine Operation Required
    private void executionInstructionDecode() {
        // Extract the opcode from the IR and use to set currentInstruction
        switch(extractOpCode(this.ir)) {
            case 0:
                this.currentInstruction = new Halt(this.ir, this.context);
                break;
            case 1:
                this.currentInstruction = new LoadRegisterFromMemory(this.ir, this.context);
                break;
            case 2:
                this.currentInstruction = new StoreRegisterToMemory(this.ir, this.context);
                break;
            case 3:
                this.currentInstruction = new LoadRegisterWithAddress(this.ir, this.context);
                break;
            case 4:
                this.currentInstruction = new AddMemoryToRegister(this.ir, this.context);
                break;
            case 5:
                this.currentInstruction = new SubtractMemoryFromRegister(this.ir, this.context);
                break;
            case 6:
                this.currentInstruction = new AddImmediateToRegister(this.ir, this.context);
                break;
            case 7:
                this.currentInstruction = new SubtractImmediateFromRegister(this.ir, this.context);
                break;
            case 10:
                this.currentInstruction = new JumpIfZero(this.ir, this.context);
                break;
            case 11:
                this.currentInstruction = new JumpIfNotEqual(this.ir, this.context);
                break;
            case 12:
                this.currentInstruction = new JumpIfConditionCode(this.ir, this.context);
                break;
            case 13:
                this.currentInstruction = new UnconditionalJumpToAddress(this.ir, this.context);
                break;
            case 14:
                this.currentInstruction = new JumpAndSaveReturnAddress(this.ir, this.context);
                break;
            case 15:
                this.currentInstruction = new ReturnFromSubroutine(this.ir, this.context);
                break;
            case 16:
                this.currentInstruction = new SubtractOneAndBranch(this.ir, this.context);
                break;
            case 17:
                this.currentInstruction = new JumpGreaterThanOrEqualTo(this.ir, this.context);
                break;
            case 20:
                this.currentInstruction = new MultiplyRegisterByRegister(this.ir, this.context);
                break;
            case 21:
                this.currentInstruction = new DivideRegisterByRegister(this.ir, this.context);
                break;
            case 22:
                this.currentInstruction = new TestTheEqualityOfRegisterAndRegister(this.ir, this.context);
                break;
            case 23:
                this.currentInstruction = new LogicalAndOfRegisterAndRegister(this.ir, this.context);
                break;
            case 24:
                this.currentInstruction = new LogicalOrOfRegisterAndRegister(this.ir, this.context);
                break;
            case 25:
                this.currentInstruction = new LogicalNotOfRegisterAndRegister(this.ir, this.context);
                break;
            case 30:
                this.currentInstruction = new Trap(this.ir, this.context);
                break;
            case 31:
                this.currentInstruction = new ShiftRegisterByCount(this.ir, this.context);
                break;
            case 32:
                this.currentInstruction = new RotateRegisterByCount(this.ir, this.context);
                break;
            case 33:
                this.currentInstruction = new FloatingAddMemoryToRegister(this.ir, this.context);
                break;
            case 34:
                this.currentInstruction = new FloatingSubtractMemoryFromRegister(this.ir, this.context);
                break;
            case 35:
                this.currentInstruction = new VectorAdd(this.ir, this.context);
                break;
            case 36:
                this.currentInstruction = new VectorSubtract(this.ir, this.context);
                break;
            case 37:
                this.currentInstruction = new ConvertToFixedOrFloatingPoint(this.ir, this.context);
                break;
            case 41:
                this.currentInstruction = new LoadIndexRegisterFromMemory(this.ir, this.context);
                break;
            case 42:
                this.currentInstruction = new StoreIndexRegisterToMemory(this.ir, this.context);
                break;
            case 50:
                this.currentInstruction = new LoadFloatingPointFromMemory(this.ir, this.context);
                break;
            case 51:
                this.currentInstruction = new StoreFloatingPointToMemory(this.ir, this.context);
                break;
            case 61:
                this.currentInstruction = new InputCharacterToRegisterFromDevice(this.ir, this.context);
                break;
            case 62:
                this.currentInstruction = new OutputCharacterToDeviceFromRegister(this.ir, this.context);
                break;
            case 63:
                this.currentInstruction = new CheckDeviceStatusToRegister(this.ir, this.context);
                break;
        }
    }

    public void startExecutionLoop(){
        this.context.msr.setIsRunning(true);
        while(this.context.msr.isRunning()){
            singleStep();
        }
    }

    public void pauseExecutionLoop(){
        this.context.msr.setIsRunning(false);
    }
}
