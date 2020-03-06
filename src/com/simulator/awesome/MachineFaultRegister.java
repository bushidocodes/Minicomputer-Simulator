package com.simulator.awesome;

import static com.simulator.awesome.Utils.*;

// Machine Fault Register.
// Contains the ID code of a machine fault after it occurs
// DO NOT IMPLEMENT UNTIL PHASE 3
// ID   |   Fault
// 0001 |   Illegal Memory Address to Reserved Locations
// 0010 |   Illegal TRAP code
// 0100 |   Illegal Operation Code
// 1000 |   Illegal Memory Address beyond Config.WORD_COUNT - 1
public class MachineFaultRegister {
    private byte mfr;

    MachineFaultRegister(){
        this.mfr = 0;
    }

    public byte get() { return this.mfr; };

    public boolean isIllegalMemoryAccessToReservedLocations() {
        return getNthLeastSignificantBit(this.mfr, 0);
    }

    public void setIllegalMemoryAccessToReservedLocations(boolean isIllegalMemoryAccessToReservedLocations) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 0, isIllegalMemoryAccessToReservedLocations);
    }

    public boolean isIllegalTrapCode() {
        return getNthLeastSignificantBit(this.mfr, 1);
    }

    public void setIllegalTrapCode(boolean isIllegalTrapCode) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 1, isIllegalTrapCode);
    }

    public boolean isIllegalOpcode() {
        return getNthLeastSignificantBit(this.mfr, 2);
    }

    public void setIsIllegalOpcode(boolean isIllegalOpcode) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 2, isIllegalOpcode);
    }

    public boolean isIllegalMemoryAddressBeyondLimit() {
        return getNthLeastSignificantBit(this.mfr, 3);
    }

    public void setIsIllegalMemoryAddressBeyondLimit(boolean isIllegalMemoryAddressBeyondLimit) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 3, isIllegalMemoryAddressBeyondLimit);
    }

    public String toString(){
        return wordToString(this.get()).substring(12,16);
    }
}
