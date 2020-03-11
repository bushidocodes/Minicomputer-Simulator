package com.simulator.awesome;

import static com.simulator.awesome.Utils.*;

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
// 0000000000100000 | Fault Handler Flag
// 0000000001000000 | Supervisor Fault Flag
// 0000000110000000 | Call Stack Depth Bits
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
        this.msr = (short)setNthLeastSignificantBit(this.msr, 0, isSupervisorMode);
    }

    public boolean isReadyForInput(){
        return getNthLeastSignificantBit(this.msr, 1);
    }

    public void setReadyForInput(boolean isReadyForInput){
        this.msr = (short)setNthLeastSignificantBit(this.msr, 1, isReadyForInput);
    }

    public boolean isRunning(){
        return getNthLeastSignificantBit(this.msr, 2);
    }

    public void setIsRunning(boolean isRunning){
        this.msr = (short)setNthLeastSignificantBit(this.msr, 2, isRunning);
    }

    public boolean isInteractive(){
        return getNthLeastSignificantBit(this.msr, 3);
    }

    public void setIsInteractive(boolean isInteractive){
        this.msr = (short)setNthLeastSignificantBit(this.msr, 3, isInteractive);
    }

    public boolean isDebugging(){
        return getNthLeastSignificantBit(this.msr, 4);
    }

    public void setIsDebugging(boolean isDebugging){
        this.msr = (short)setNthLeastSignificantBit(this.msr, 4, isDebugging);
    }

    public boolean isExecutingFaultHandler(){
        return getNthLeastSignificantBit(this.msr, 5);
    }

    public void setIsExecutingFaultHandler(boolean isExecutingFaultHandler){
        this.msr = (short)setNthLeastSignificantBit(this.msr, 5, isExecutingFaultHandler);
    }

    public boolean isSupervisorFault(){
        return getNthLeastSignificantBit(this.msr, 6);
    }

    public void setIsSupervisorFault(boolean isSupervisorFault){
        this.msr = (short)setNthLeastSignificantBit(this.msr, 6, isSupervisorFault);
    }

    public short getCallStackDepth() {
        return getNthLeastSignificantBits(this.msr,7,2);
    }

    public void setCallStackDepth(short value) {
        this.msr = setNthLeastSignificantBits(this.msr,7,2, value);
    }

    public void reset() {
        boolean isInteractive = this.isInteractive();
        boolean isDebugging = this.isDebugging();
        this.msr = 0;
        this.setIsInteractive(isInteractive);
        this.setIsDebugging(isDebugging);

    }
}
