package com.simulator.awesome;

import java.io.File;
import java.lang.Short;
import java.lang.String;
import java.util.Arrays;

class Main {

    public static void main(String[] args) {
        Simulator my_compy = new Simulator(2048);

        // Manually set a word
        my_compy.memory[0] = Short.valueOf((short)0b101);

        // And print it out to console
        System.out.println("Word 0: " + Simulator.word_to_string(my_compy.memory[0]));

        // Set a word with opcode 0... halt
        my_compy.memory[1] = Short.valueOf((short)0b0000000000000000);
        Simulator.parse_and_execute(my_compy.memory[1]);

        // Set a word with opcode 1... LDR
        my_compy.memory[1] = Short.valueOf((short)0b0000100000000000);
        Simulator.parse_and_execute(my_compy.memory[1]);

        // Execute a word with opcode 2... STR
        my_compy.memory[1] = Short.valueOf((short)0b0001000000000000);
        Simulator.parse_and_execute(my_compy.memory[1]);

        Assembler my_assembler = new Assembler();
        String basePath = new File("").getAbsolutePath(); //get current base directory
        my_assembler.loadFile(basePath.concat("/static/sample-program.txt"));
        System.out.println(Arrays.toString(my_assembler.convertToMachineCode()));
    }
}
