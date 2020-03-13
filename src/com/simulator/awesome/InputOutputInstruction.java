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

    public void validateInputDevice(short devid){
        // Input from console printer triggers a fault
        if (devid == 1 || devid < 0 || devid > 31) {
            this.didFault = true;
        }
    }

    public void validateOutputDevice(short devid){
        // Output to console keyboard or card reader triggers a fault
        if (devid == 0 || devid == 2 || devid < 0 || devid > 31) {
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
        // If the buffer is empty, pause the execution loop and wait for user input
        if (this.context.io.isInputBufferNull(this.deviceId)) {
            this.context.msr.setReadyForInput(true);
            this.context.cu.pauseExecutionLoop();
        }
    }

    public void execute(){
        // c(Register) <- inputBuffer <- Device
        short word = this.context.io.getFirstWordFromInputBuffer(this.deviceId);
        this.context.setGeneralRegister(this.registerId, word);
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
        this.context.io.addWordToOutputBuffer(this.deviceId,this.context.getGeneralRegister(this.registerId));
    }
}

/**
 OPCODE 63 - Check Device Status to Register
 Octal: 077
 CHK r, devid
 r = 0..3
 c(r) <- device status (size of the device's buffer)
 */
class CheckDeviceStatusToRegister extends InputOutputInstruction {
    public CheckDeviceStatusToRegister(short word, Simulator context) {
        super(word, context);
        validateGeneralRegisterIndex(this.registerId);
        switch(this.deviceId){
            case 0: // Console Keyboard
            case 2: // Card Reader
                validateInputDevice(this.deviceId);
                break;
            case 1: // Console Printer
                validateOutputDevice(this.deviceId);
                break;
            default: // Other devices are not specified and therefore could be input or output
                validateInputDevice(this.deviceId);
                validateOutputDevice(this.deviceId);
                break;
        }
    }
    public void execute(){
        // If we know the type of device, check the corresponding buffer. If we don't know, take whichever buffer is non-zero.
        switch(this.deviceId){
            case 0: // Console Keyboard
                this.context.setGeneralRegister(this.registerId, (short) this.context.io.getSizeOfInputBuffer(this.deviceId));
                break;
            case 2: // Card Reader
                // c(Register) <- size of inputBuffer
                // this will break if the inputBuffer has more than 32,767 items in it due to casting an int to short.
                this.context.setGeneralRegister(this.registerId, (short) this.context.io.getSizeOfInputBuffer(this.deviceId));
                break;
            case 1: // Console Printer
                // c(Register) <- size of outputBuffer
                // this will break if the outputBuffer has more than 32,767 items in it due to casting an int to short.
                this.context.setGeneralRegister(this.registerId, (short) this.context.io.getSizeOfOutputBuffer(this.deviceId));
                break;
            default: // Other devices are not specified and therefore could be input or output
                if (context.io.isInputBufferNull(this.deviceId)){
                    if (context.io.isOutputBufferNull(this.deviceId)){ // if both are empty, just return 0
                        this.context.setGeneralRegister(this.registerId, (short) 0);
                    } else {
                        // outputBuffer is non-zero, c(Register) <- size of outputBuffer
                        this.context.setGeneralRegister(this.registerId, (short) this.context.io.getSizeOfOutputBuffer(this.deviceId));
                    }
                } else { // inputBuffer is non-zero, c(Register) <- size of inputBuffer
                    this.context.setGeneralRegister(this.registerId, (short) this.context.io.getSizeOfInputBuffer(this.deviceId));
                }
                break;
        }
    }

    public void storeResult(){
        // NOOP
    }
}