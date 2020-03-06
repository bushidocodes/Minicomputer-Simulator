package com.simulator.awesome;

import static com.simulator.awesome.Utils.wordToString;

public class MemoryAddressRegister {
    private short mar;
    static final short MAR_MASK = (short)0b0000111111111111;

    MemoryAddressRegister(){
        this.mar = 0;
    }

    public short get() {
        return (short) (this.mar & MAR_MASK);
    }

    public void set(short unmaskedMAR) {
        this.mar = (short) (unmaskedMAR & MAR_MASK);
    }

    public String toString(){
        return wordToString(this.get()).substring(4,16);
    }

}
