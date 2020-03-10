package com.simulator.awesome;

public class ControlUnit {
    final Simulator context;

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

    public void handleFault(){
        // Set inFault flag on MSR
        this.context.msr.setIsExecutingFaultHandler(true);
        // If in supervisor mode, set faultInSupervisor flag. Else, Supervisor Mode
        if (this.context.msr.isSupervisorMode()){
            this.context.msr.setIsSupervisorFault(true);
        } else {
            this.context.msr.setSupervisorMode(true);
        }
        try {
            // Save PC to address 4
            this.context.memory.store((short)4, this.context.pc.get());
            // Save R0 to address 5
            this.context.memory.store((short)5, this.context.getGeneralRegister((short)0));
        } catch (IllegalMemoryAccessToReservedLocationsException e) {
            e.printStackTrace();
        } catch (IllegalMemoryAddressBeyondLimitException e) {
            e.printStackTrace();
        }

        // Copy The Fault ID to R0
        if (this.context.mfr.isIllegalMemoryAccessToReservedLocations()){
            this.context.setGeneralRegister((short)0, (short) 0);
        } else if (this.context.mfr.isIllegalTrapCode()) {
            this.context.setGeneralRegister((short)0, (short) 1);
        } else if (this.context.mfr.isIllegalOpcode()){
            this.context.setGeneralRegister((short)0, (short) 2);
        } else if (this.context.mfr.isIllegalMemoryAddressBeyondLimit()){
            this.context.setGeneralRegister((short)0, (short) 3);
        } else {
            System.out.println("Unknown Error: " + this.context.mfr.get());
        }

        // Set Program Counter to 1
        try {
            this.context.pc.set(this.context.memory.fetch((short)1));
        } catch (IllegalMemoryAccessToReservedLocationsException e) {
            e.printStackTrace();
        } catch (IllegalMemoryAddressBeyondLimitException e) {
            e.printStackTrace();
        }

        // Set Execution Step to 1
        this.executionStep = 1;
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
        try {
            switch (this.executionStep) {
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
                    /*
                     In 1 cycle, move the contents of the IRR to:
                     a. If target = register, use Register Select 1 to store IRR contents into the specified register
                     b. If target = memory, such as a STR, move contents of IRR to MBR. On the next cycle, move contents of MBR to memory using address in MAR.
                     */
                    this.currentInstruction.storeResult();
                    break;
            }
            if (this.executionStep == 5) {
                // Commenting out because this causing the program to hang
                if (this.context.msr.isDebugging()) {
                    this.context.dumpRegistersToJavaConsole();
                    this.context.memory.dump();
                }

                this.executionStep = 1;
            } else {
                this.executionStep++;
            }
        } catch (IllegalMemoryAccessToReservedLocationsException e) {
            this.context.io.engineerConsolePrintLn(e.getMessage());
            this.context.mfr.setIllegalMemoryAccessToReservedLocations(true);
            this.handleFault();
        } catch (IllegalMemoryAddressBeyondLimitException e) {
            this.context.io.engineerConsolePrintLn(e.getMessage());
            this.context.mfr.setIsIllegalMemoryAddressBeyondLimit(true);
            this.handleFault();
        } catch (IllegalOperationCodeException e) {
            this.context.io.engineerConsolePrintLn(e.getMessage());
            this.context.mfr.setIsIllegalOpcode(true);
            this.handleFault();
        } catch (IllegalTrapCodeException e) {
            this.context.io.engineerConsolePrintLn(e.getMessage());
            this.context.mfr.setIllegalTrapCode(true);
            this.handleFault();

        }
    }

    // Execution Step 1
    // Obtain Instruction from Program Storage
    private void executionInstructionFetch() throws IllegalMemoryAccessToReservedLocationsException, IllegalMemoryAddressBeyondLimitException {
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
    private void executionInstructionDecode() throws IllegalOperationCodeException {
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
            default:
                throw new IllegalOperationCodeException(extractOpCode(this.ir) + "is an invalid OPCODE");
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
