package com.simulator.awesome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OutputInstructionTest {

    private Simulator sim;

    @BeforeEach
    void setUp() {
        sim = new Simulator(Config.WORD_COUNT);
        sim.msr.setSupervisorMode(true);
        sim.io.initializeIOBuffers(); // ensure queues are initialised (issue #113)
    }

    // Encode an OUT instruction: opcode=111110 (0x3E), r=reg, devid=device
    // Bit layout: opcode(6) | r(2) | 000 | devid(5)
    private short encodeOut(int reg, int devid) {
        return (short) ((0b111110 << 10) | (reg << 8) | devid);
    }

    @Test
    void constructor_doesNotEmitToOutputBuffer() {
        // Load register 0 with a value
        sim.setGeneralRegister((short) 0, (short) 65); // 'A'
        // Device 1 = console printer (valid output device)
        short word = encodeOut(0, 1);

        // Creating the instruction must NOT add anything to the buffer yet
        OutputCharacterToDeviceFromRegister inst =
            new OutputCharacterToDeviceFromRegister(word, sim);

        assertTrue(sim.io.isOutputBufferNull((short) 1),
            "output buffer must be empty after construction — output happens in execute(), not the constructor");
    }

    @Test
    void execute_emitsRegisterValueToOutputBuffer() {
        sim.setGeneralRegister((short) 0, (short) 66); // 'B'
        short word = encodeOut(0, 1);
        OutputCharacterToDeviceFromRegister inst =
            new OutputCharacterToDeviceFromRegister(word, sim);

        inst.execute();

        assertFalse(sim.io.isOutputBufferNull((short) 1),
            "output buffer should have one entry after execute()");
        assertEquals((short) 66, sim.io.getFirstWordFromOutputBuffer((short) 1));
    }

    @Test
    void execute_withFault_doesNotEmit() {
        sim.setGeneralRegister((short) 0, (short) 67);
        short word = encodeOut(0, 1);
        OutputCharacterToDeviceFromRegister inst =
            new OutputCharacterToDeviceFromRegister(word, sim);
        inst.didFault = true; // simulate a fault

        inst.execute();

        assertTrue(sim.io.isOutputBufferNull((short) 1),
            "faulted instruction must not emit any output");
    }
}
