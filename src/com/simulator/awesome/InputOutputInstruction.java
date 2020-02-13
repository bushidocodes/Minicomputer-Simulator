package com.simulator.awesome;

// This class centralizes the parsing of the LoadStore class of instructions
// It is used by IN, OUT, CHK
public class InputOutputInstruction extends Instruction {
    public short registerId;
    public short deviceId;

    InputOutputInstruction(short word, Simulator context) {
        super(word, context);
        short registerMask           = (short) 0b0000001100000000;
        short deviceMask             = (short) 0b0000000000111111;

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
    }
    public void execute(){
        System.out.println("IN");
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
    }
    public void execute(){
        System.out.println("OUT");
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
