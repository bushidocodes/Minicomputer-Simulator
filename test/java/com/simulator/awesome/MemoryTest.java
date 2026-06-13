package com.simulator.awesome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {

    private Simulator sim;
    private Memory memory;

    @BeforeEach
    void setUp() {
        sim = new Simulator();
        sim.msr.setSupervisorMode(true); // bypass protection checks for direct tests
        memory = sim.memory;
    }

    @Test
    void fetch_validAddress_returnsWord() throws Exception {
        memory.store((short) 100, (short) 42);
        assertEquals((short) 42, memory.fetch((short) 100));
    }

    @Test
    void fetch_addressEqualToWordCount_throwsBeyondLimit() {
        // address == wordCount is the first out-of-bounds address (issue #114)
        short oobAddress = memory.getWordCount();
        assertThrows(IllegalMemoryAddressBeyondLimitException.class,
            () -> memory.fetch(oobAddress),
            "address == wordCount must throw, not silently succeed");
    }

    @Test
    void fetch_addressBeyondWordCount_throwsBeyondLimit() {
        short oobAddress = (short) (memory.getWordCount() + 1);
        assertThrows(IllegalMemoryAddressBeyondLimitException.class,
            () -> memory.fetch(oobAddress));
    }

    @Test
    void store_addressEqualToWordCount_throwsBeyondLimit() {
        short oobAddress = memory.getWordCount();
        assertThrows(IllegalMemoryAddressBeyondLimitException.class,
            () -> memory.store(oobAddress, (short) 0),
            "store to address == wordCount must throw");
    }

    @Test
    void fetch_lastValidAddress_doesNotThrow() throws Exception {
        short lastValid = (short) (memory.getWordCount() - 1);
        assertDoesNotThrow(() -> memory.fetch(lastValid),
            "the last valid address (wordCount-1) must be accessible");
    }
}
