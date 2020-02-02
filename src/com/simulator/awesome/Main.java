package com.simulator.awesome;

import java.io.File;
import java.lang.Short;
import java.lang.String;
import java.util.Arrays;

class Main {

    public static void main(String[] args) {
        Assembler myAssembler = new Assembler();
        String basePath = new File("").getAbsolutePath(); //get current base directory
        myAssembler.loadFile(basePath.concat("/static/sample-program.txt"));

        Simulator myComputer = new Simulator(2048);

        // Load the program into memory. You may optionally specify a location, e.g. load_program(program, address)
        myComputer.loadProgram(myAssembler.convertToMachineCode());

        // See the program loaded in memory
        System.out.println(Arrays.toString(myComputer.memoryToString()));

        LoadStoreInstruction load = new LoadStoreInstruction((short) 0b0000010111010101);
        load.print();

        ArithmeticLogicInstruction arr = new ArithmeticLogicInstruction((short) 0b0101000001000000);
        arr.print();

        ShiftRotateInstruction shift = new ShiftRotateInstruction((short) 0b0111110100001111);
        shift.print();

        InputOutputInstruction inOut = new InputOutputInstruction((short) 0b1111110100111111);
        inOut.print();


        // Set a word with opcode 0... halt. This is actually implemented.
        myComputer.setWord(12, (short) 0b0000000000000000);
        myComputer.parseAndExecute(myComputer.getWord(12));
    }
}
