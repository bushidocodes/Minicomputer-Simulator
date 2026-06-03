package com.simulator.awesome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimulatorResetTest {

    private Simulator sim;

    @BeforeEach
    void setUp() {
        sim = new Simulator(Config.WORD_COUNT);
        sim.io.initializeIOBuffers(); // workaround for issue #113
        sim.msr.setSupervisorMode(true);
    }

    @Test
    void reset_clearsFpuState() {
        // Populate FPU state before reset
        sim.fpu.setA((short) 0x0F12); // non-zero FP value
        sim.fpu.setB((short) 0x0A00);
        sim.fpu.add();
        FloatingPointUnit fpuBeforeReset = sim.fpu;

        sim.reset();
        sim.msr.setSupervisorMode(true); // re-enable for follow-up checks

        // After reset, fpu must be a fresh instance with zeroed state
        assertNotSame(fpuBeforeReset, sim.fpu,
            "reset() must replace the FPU instance");

        // getYAsShort() on a fresh FPU returns toShort() of an all-zero FloatingPointNumber
        assertEquals((short) 0, sim.fpu.getYAsShort(),
            "FPU output register must be zero after reset");
    }

    @Test
    void reset_clearsFpuRegistersInSimulator() {
        // Set the floating-point registers via Simulator
        sim.setFloatingRegister((short) 0, (short) 0x1234);
        sim.setFloatingRegister((short) 1, (short) 0x5678);

        sim.reset();
        sim.msr.setSupervisorMode(true);

        assertEquals((short) 0, sim.getFloatingRegister((short) 0),
            "FR0 must be zero after reset");
        assertEquals((short) 0, sim.getFloatingRegister((short) 1),
            "FR1 must be zero after reset");
    }

    @Test
    void reset_clearsGeneralRegisters() {
        sim.setGeneralRegister((short) 0, (short) 99);
        sim.setGeneralRegister((short) 1, (short) 77);
        sim.reset();
        assertEquals((short) 0, sim.getGeneralRegister((short) 0));
        assertEquals((short) 0, sim.getGeneralRegister((short) 1));
    }
}
