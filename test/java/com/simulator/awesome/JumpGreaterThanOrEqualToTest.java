package com.simulator.awesome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JumpGreaterThanOrEqualToTest {

    private Simulator sim;

    // JGE opcode = 010001 (17 decimal)
    // Bit layout: opcode(6) | r(2) | ix(2) | I(1) | address(5)
    private short encodeJge(int reg, int ix, int indirect, int address) {
        return (short) ((0b010001 << 10) | (reg << 8) | (ix << 6) | (indirect << 5) | address);
    }

    @BeforeEach
    void setUp() {
        sim = new Simulator(Config.WORD_COUNT);
        sim.io.initializeIOBuffers(); // workaround for issue #113
        sim.msr.setSupervisorMode(true);
    }

    private short getPC() {
        return sim.pc.get();
    }

    /** Drive the full 5-phase fetch/decode/operand-fetch/execute/store-result cycle. */
    private void executeJge(short regValue, short targetAddress) throws Exception {
        // Place a JGE r=0, ix=0, I=0, address=targetAddress instruction at address 200
        short pc = 200;
        short instrAddress = (short) targetAddress;
        short instructionWord = encodeJge(0, 0, 0, instrAddress);
        sim.memory.store(pc, instructionWord);
        sim.pc.set(pc);
        sim.setGeneralRegister((short) 0, regValue);

        // singleStep() advances one phase at a time (1=fetch … 5=storeResult)
        for (int i = 0; i < 5; i++) {
            sim.cu.singleStep();
        }
    }

    @Test
    void jge_positiveValue_branches() throws Exception {
        executeJge((short) 5, (short) 25);
        assertEquals((short) 25, getPC(), "JGE should branch when c(r) > 0");
    }

    @Test
    void jge_zero_branches() throws Exception {
        executeJge((short) 0, (short) 25);
        assertEquals((short) 25, getPC(), "JGE should branch when c(r) == 0");
    }

    @Test
    void jge_negativeValue_doesNotBranch() throws Exception {
        executeJge((short) -1, (short) 25);
        // PC was 200, the control unit increments before executing, so PC = 201 after no-branch
        assertNotEquals((short) 25, getPC(), "JGE must NOT branch when c(r) < 0");
    }

    @Test
    void jge_mostNegativeShort_doesNotBranch() throws Exception {
        executeJge(Short.MIN_VALUE, (short) 25);
        assertNotEquals((short) 25, getPC(), "JGE must NOT branch for the most-negative value");
    }
}
