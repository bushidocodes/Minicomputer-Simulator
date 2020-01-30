package com.simulator.awesome;

import java.io.File;
import java.lang.Short;
import java.lang.String;
import java.util.Arrays;

class Main {

    public static void main(String[] args) {
        Assembler my_assembler = new Assembler();
        String basePath = new File("").getAbsolutePath(); //get current base directory
        my_assembler.loadFile(basePath.concat("/static/sample-program.txt"));

        Simulator my_compy = new Simulator(2048);

        // Load the program into memory. You may optionally specify a location, e.g. load_program(program, address)
        my_compy.load_program(my_assembler.convertToMachineCode());

        // See the program loaded in memory
        System.out.println(Arrays.toString(my_compy.memory_to_string()));

        // Set a word with opcode 0... halt. This is actually implemented.
        my_compy.memory[1] = Short.valueOf((short)0b0000000000000000);
        Simulator.parse_and_execute(my_compy.memory[1]);
    }
}
