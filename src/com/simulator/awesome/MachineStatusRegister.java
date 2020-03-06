package com.simulator.awesome;

import static com.simulator.awesome.Utils.getNthLeastSignificantBit;
import static com.simulator.awesome.Utils.setNthLeastSignificantBit;

// Machine Status Register
// 16 bits used as flags for system conditions
// There seems to be some overlap with the condition code register
// The main difference seems to be that the JCC exposes the four basic condition codes publicly for branching logic
// This is use for internal state
// See https://en.wikipedia.org/wiki/Status_register for more information
// 0000000000000001 | Supervisor Flag
// 0000000000000010 | Ready for I/O Flag
// 0000000000000100 | System Running Flag
// 0000000000001000 | Interactive Flag
// 0000000000010000 | Debug Flag
// All others reserved
public class MachineStatusRegister {
    private short msr;

    MachineStatusRegister(){
        this.msr = 0;
    }

    public boolean isSupervisorMode() {
        return getNthLeastSignificantBit(this.msr, 0);
    }

    public void setSupervisorMode(boolean isSupervisorMode) {
        this.msr = (byte)setNthLeastSignificantBit(this.msr, 0, isSupervisorMode);
    }

    public boolean isReadyForInput(){
        return getNthLeastSignificantBit(this.msr, 1);
    }

    public void setReadyForInput(boolean isReadyForInput){
        this.msr = (byte)setNthLeastSignificantBit(this.msr, 1, isReadyForInput);
    }

    public boolean isRunning(){
        return getNthLeastSignificantBit(this.msr, 2);
    }

    public void setIsRunning(boolean isRunning){
        this.msr = (byte)setNthLeastSignificantBit(this.msr, 2, isRunning);
    }

    public boolean isInteractive(){
        return getNthLeastSignificantBit(this.msr, 3);
    }

    public void setIsInteractive(boolean isInteractive){
        this.msr = (byte)setNthLeastSignificantBit(this.msr, 3, isInteractive);
    }

    public boolean isDebugging(){
        return getNthLeastSignificantBit(this.msr, 4);
    }

    public void setIsDebugging(boolean isDebugging){
        this.msr = (byte)setNthLeastSignificantBit(this.msr, 4, isDebugging);
    }
}
