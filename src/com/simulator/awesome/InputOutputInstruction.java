package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by IN, OUT, CHK
public class InputOutputInstruction extends Instruction {
    public short registerId;
    public short deviceId;

    InputOutputInstruction(short word, Simulator context) {
        super(word, context);
        short registerMask           = (short) 0b0000001100000000;
        short deviceMask             = (short) 0b0000000000011111;

        short deviceOffset           = 0;
        short registerOffset         = 8;

        this.deviceId = Utils.short_unsigned_right_shift((short)(word & deviceMask), deviceOffset);
        this.registerId = Utils.short_unsigned_right_shift((short)(word & registerMask), registerOffset);
    }

    public void fetchOperand(){
        // NOOP
    }

    public void execute() {
        // NOOP
    }

    public void storeResult(){
        // NOOP
    }

    public void validateInputDevice(short devid){
        // Input from console printer triggers a fault
        if (devid == 1) {
            this.didFault = true;
        }
    }

    public void validateOutputDevice(short devid){
        // Output to console keyboard or card reader triggers a fault
        if (devid == 0 || devid == 2) {
            this.didFault = true;
        }
    }

    public void print(){
        System.out.println("OpCode: " + Short.toUnsignedInt(this.opCode));
        System.out.println("Register ID: " + this.registerId);
        System.out.println("Device ID: " + this.deviceId);
    }
}


/**
 * OPCODE 61 - Input Character To Register from Device
 * Octal: 075
 * IN r, devid
 * r = 0..3
 */
class InputCharacterToRegisterFromDevice extends InputOutputInstruction {
    public InputCharacterToRegisterFromDevice(short word, Simulator context) {
        super(word, context);
        validateGeneralRegisterIndex(this.registerId);
        validateInputDevice(this.deviceId);
    }
    public void fetchOperand(){
        // Pause the execution loop and wait for user input
        this.context.setReadyForInput(true);
        this.context.pauseExecutionLoop();
    }

    public void execute(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // c(Register) <- inputBuffer <- Device
        this.context.setGeneralRegister(this.registerId,this.context.getFirstWordFromInputBuffer(this.deviceId));
    }

    public void storeResult(){
        // NOOP
    }
}

/**
 OPCODE 62 - Output Character to Device from Register
 Octal: 076
 OUT r, devid
 r = 0..3
 */
class OutputCharacterToDeviceFromRegister extends InputOutputInstruction {
    public OutputCharacterToDeviceFromRegister(short word, Simulator context) {
        super(word, context);
        validateGeneralRegisterIndex(this.registerId);
        validateOutputDevice(this.deviceId);
    }
    public void execute(){
        // Fault Handling and Validation
        if (this.didFault) return;

        // Device <- outputBuffer <- c(Register)
        this.context.addWordToOutputBuffer(this.deviceId,this.context.getGeneralRegister(this.registerId));
    }

    public void storeResult(){
        // NOOP
    }
}

/**
 OPCODE 63 - Check Device Status to Register
 Octal: 077
 CHK r, devid
 r = 0..3
 c(r) <- device status
 */
class CheckDeviceStatusToRegister extends InputOutputInstruction {
    public CheckDeviceStatusToRegister(short word, Simulator context) {
        super(word, context);
    }
    public void execute(){
        System.out.println("CHK");
    }

    public void storeResult(){
        // NOOP
    }
}
