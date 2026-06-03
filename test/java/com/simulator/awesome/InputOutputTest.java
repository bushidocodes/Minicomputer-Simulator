package com.simulator.awesome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InputOutputTest {

    private Simulator sim;
    private InputOutput io;

    @BeforeEach
    void setUp() {
        sim = new Simulator(Config.WORD_COUNT);
        io = sim.io;
    }

    // These all threw NullPointerException before the fix because the individual
    // LinkedBlockingQueue elements were never created inside the array.

    @Test
    void outputBuffer_isNullCheck_doesNotThrow() {
        assertDoesNotThrow(() -> io.isOutputBufferNull((short) 1),
            "isOutputBufferNull must not NPE on a freshly constructed InputOutput");
    }

    @Test
    void inputBuffer_isNullCheck_doesNotThrow() {
        assertDoesNotThrow(() -> io.isInputBufferNull((short) 0),
            "isInputBufferNull must not NPE on a freshly constructed InputOutput");
    }

    @Test
    void addWordToOutputBuffer_doesNotThrow() {
        assertDoesNotThrow(() -> io.addWordToOutputBuffer((short) 1, (short) 42),
            "addWordToOutputBuffer must not NPE on a freshly constructed InputOutput");
    }

    @Test
    void addAndRetrieve_outputBuffer() {
        io.addWordToOutputBuffer((short) 1, (short) 99);
        assertFalse(io.isOutputBufferNull((short) 1));
        assertEquals((short) 99, io.getFirstWordFromOutputBuffer((short) 1));
    }

    @Test
    void addAndRetrieve_inputBuffer() {
        io.addWordToInputBuffer((short) 0, (short) 65);
        assertFalse(io.isInputBufferNull((short) 0));
        assertEquals((short) 65, io.getFirstWordFromInputBuffer((short) 0));
    }

    @Test
    void getSizeOfOutputBuffer_returnsZeroInitially() {
        assertEquals(0, io.getSizeOfOutputBuffer((short) 3));
    }

    @Test
    void getSizeOfInputBuffer_returnsZeroInitially() {
        assertEquals(0, io.getSizeOfInputBuffer((short) 3));
    }
}
