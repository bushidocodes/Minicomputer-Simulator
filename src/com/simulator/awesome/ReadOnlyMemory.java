package com.simulator.awesome;

import java.io.File;

// Performs initial load of addresses, traps, etc.
public class ReadOnlyMemory {
    final Simulator context;
    ReadOnlyMemory(Simulator context){
        this.context = context;
    }

    public void load(){
        try {
            String basePath = new File("").getAbsolutePath(); //get current base directory
            // Our logic to load programs right to left assumes the memoryPosition is exclusive (the start address of the subsequent program), so add one to wordcount to use last word
            short earliestTopAddressUsed = Config.WORD_COUNT;

            // Set privileged
            this.context.msr.setSupervisorMode(true);

            // Pushing the IPL button loads the based addresses and ascii tables, ready for a user program to be loaded
            Assembler assembler = new Assembler();

            // Set all Supervisor Functions at the Uppermost addresses (ideally in reverse order)

            // Trap Table
            assembler.loadFile(basePath.concat("/static/trap-table.txt"));
            String[] trapTable = assembler.convertToMachineCode();
            short trapTableLocation = context.loadProgram(trapTable, earliestTopAddressUsed, false, false);
            earliestTopAddressUsed = trapTableLocation;

            // Load the traps
            // We skip 15 to have an "Invalid Trap" to test the error handling in the fault-demo program!
            for (int i = 14; i >= 0; i--){
                assembler.loadFile(basePath.concat("/static/trap-" + i + ".txt"));
                String[] trap = assembler.convertToMachineCode();
                short trapLocation = context.loadProgram(trap, earliestTopAddressUsed, false, false);
                this.context.memory.store((short)(trapTableLocation + i), trapLocation);
                earliestTopAddressUsed = trapLocation;
            }

            // Fault Handler
            assembler.loadFile(basePath.concat("/static/fault-handler.txt"));
            String[] faultHandler = assembler.convertToMachineCode();
            short faultHandlerLocation = context.loadProgram(faultHandler, earliestTopAddressUsed, false, false);
            earliestTopAddressUsed = faultHandlerLocation;

            // Bootloader
            assembler.loadFile(basePath.concat("/static/bootloader.txt"));
            String[] bootloader = assembler.convertToMachineCode();
            short bootloaderLocation = context.loadProgram(bootloader, earliestTopAddressUsed, false, false);
            earliestTopAddressUsed = bootloaderLocation;

            // Set the base address of the Supervisor Functions for Memory Protection
            this.context.memory.baseUpperProtectedMemory = earliestTopAddressUsed;

            // Set User Library Functions

            // print-int
            assembler.loadFile(basePath.concat("/static/print-int.txt"));
            String[] printInt = assembler.convertToMachineCode();
            short printIntLocation = context.loadProgram(printInt, earliestTopAddressUsed, false, false);
            earliestTopAddressUsed = printIntLocation;

            // read-int
            assembler.loadFile(basePath.concat("/static/read-int.txt"));
            String[] readInt = assembler.convertToMachineCode();
            short readIntLocation = context.loadProgram(readInt, earliestTopAddressUsed, false, false);
            earliestTopAddressUsed = readIntLocation;

            // print-string
            assembler.loadFile(basePath.concat("/static/print-string.txt"));
            String[] printString = assembler.convertToMachineCode();
            short printStringLocation = context.loadProgram(printString, earliestTopAddressUsed, false, false);
            earliestTopAddressUsed = printStringLocation;

            // read-string
            assembler.loadFile(basePath.concat("/static/read-string.txt"));
            String[] readString = assembler.convertToMachineCode();
            short readStringLocation = context.loadProgram(readString, earliestTopAddressUsed, false, false);
            earliestTopAddressUsed = readStringLocation;

            this.context.memory.baseUpperReadOnlyMemory = earliestTopAddressUsed;

            // Address 0: Trap Table. Place at end of memory and put indirect at 0
            this.context.memory.store((short) 0, trapTableLocation);
            // Address  1: Indirect to Machine Fault Handler
            this.context.memory.store((short) 1, faultHandlerLocation);
            // Address  2: Store PC for Trap
            // Address  3: Reserved for Supervisor
            // Address  4: Store PC for Machine Fault
            // Address  5: Reserved for Supervisor
            // Address  6: Indirect to Bootloader
            this.context.memory.store((short) 6, bootloaderLocation);
            // Address  7: Indirect to User Program
            // Address  8: Reserved for Supervisor
            // Address  9: Reserved for Supervisor
            // Address 10: Reserved for Supervisor
            // Address 11: Reserved for Supervisor
            // Address 12: Reserved for Supervisor
            // Address 13: Reserved for Supervisor
            // Address 14: Reserved for Supervisor
            // Address 15: Reserved for Supervisor
            // Address 16: Current Stack Frame Indirect
            // Address 17: Register-to-Register Buffer (for copying index registers to general registers and visa-versa)
            // Address 18: Heap Dataset Indirect
            // Address 19: Reserved for User
            // Address 20: Reserved for User
            // Address 21: Reserved for User
            // Address 22: Reserved for User
            // Address 23: Reserved for User
            // Address 24: Indirect to the read-string subroutine
            this.context.memory.store((short) 24, readStringLocation);
            // Address 25: Indirect to the print-string subroutine
            this.context.memory.store((short) 25, printStringLocation);
            // Address 26: Indirect to the read-int subroutine
            this.context.memory.store((short) 26, readIntLocation);
            // Address 27: Indirect to the print-int subroutine
            this.context.memory.store((short) 27, printIntLocation);
            // Address 28: 32 (Pointer to stack frame 0)
            this.context.memory.store((short)28, (short)0b0000000000100000);
            // Address 29: 64 (Pointer to stack frame 1)
            this.context.memory.store((short)29, (short)0b0000000001000000);
            // Address 30: 96 (Pointer to stack frame 2)
            this.context.memory.store((short)30, (short)0b0000000001100000);
            // Address 31: 128 (Pointer to stack frame 3)
            this.context.memory.store((short)31, (short)0b0000000010000000);

            // Note: the stack frame pointers can also be used to directly address ASCII character codes 0-127
            // https://www.ascii-code.com/
            // To load 0-31 into a register, use LDA r,0,[0-31]
            // To load 32-63 into a register, use LDA r,28,[0-31]
            // To load 64-95 into a register, use LDA r,29,[0-31]
            // To load 96-127 into a register, use LDA r,30,[0-31]

            // Start Bootloader
            this.context.pc.set(bootloaderLocation);
            this.context.cu.startExecutionLoop();

        } catch (IllegalMemoryAccessToReservedLocationsException e) {
            e.printStackTrace();
        } catch (IllegalMemoryAddressBeyondLimitException e) {
            e.printStackTrace();
        }
    }
}
