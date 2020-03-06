package com.simulator.awesome;

import static com.simulator.awesome.Utils.wordToString;

/**
 * Program Counter: Address of the next instruction to be executed
 */
public class ProgramCounter {

    // Uses the least significant 12 bits
    private short pc;
    static final short PC_MASK = (short)0b0000111111111111;

    ProgramCounter(){
        this.pc = 0;
    }

    public short get() {
        return (short) (this.pc & PC_MASK);
    }

    public void set(short unmaskedPC) {
        this.pc = (short) (unmaskedPC & PC_MASK);
    }

    public void increment() {
        this.pc++;
    }

    public String toString(){
        return wordToString(this.get()).substring(4,16);
    }
}
