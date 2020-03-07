package com.simulator.awesome;

import java.io.File;

// Performs initial load of addresses, traps, etc.
public class ReadOnlyMemory {
    Simulator context;
    ReadOnlyMemory(Simulator context){
        this.context = context;
    }

    public void load(){
        // Pushing the IPL button loads the based addresses and ascii tables, ready for a user program to be loaded
        Assembler assembler = new Assembler();

        // Address 28: Indirect to the print-int subroutine
        String basePath = new File("").getAbsolutePath(); //get current base directory
        assembler.loadFile(basePath.concat("/static/print-int.txt"));
        assembler.convertToMachineCode();
        short printIntLocation = context.loadProgram(assembler.output_arr, (short) -330, false);
        this.context.memory.store((short) 28, printIntLocation);

        // Addresses 29-31: ASCII Base Addresses to directly address character codes 0-127
        // https://www.ascii-code.com/
        // To load 0-31 into a register, use LDA r,0,[0-31]
        // To load 32-63 into a register, use LDA r,29,[0-31]
        // To load 64-95 into a register, use LDA r,30,[0-31]
        // To load 96-127 into a register, use LDA r,31,[0-31]
        this.context.memory.store((short)29, (short)0b0000000000100000);
        this.context.memory.store((short)30, (short)0b0000000001000000);
        this.context.memory.store((short)31, (short)0b0000000001100000);

        // ZOMBIE CONTENT JUST PROVIDED TO SHOW
        // Load Base Addresses representing every 32nd address from as a DataSet
        // Place it at the topmost addresses
//                String basePath = new File("").getAbsolutePath(); //get current base directory
//                assembler1.loadFile(basePath.concat("/static/base-addresses.txt"));

//                short baseAddressTableLocation = context.loadProgram(assembler1.input_arr, (short) -1, true);
        // Assign Indirect to this dataset to address 30;
//                context.memory.store((short)30, baseAddressTableLocation);
    }
}
