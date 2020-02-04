package com.simulator.awesome;

import javax.swing.*;
import java.io.File;
import java.lang.String;
import java.util.Arrays;

class Main {
    public static void main(String[] args) {
        try {
            Assembler myAssembler = new Assembler();
            String basePath = new File("").getAbsolutePath(); //get current base directory
            myAssembler.loadFile(basePath.concat("/static/sample-program.txt"));

            Simulator myComputer = new Simulator(2048);

            // Load the program into memory. You may optionally specify a location, e.g. load_program(program, address)
            myComputer.loadProgram(myAssembler.convertToMachineCode());

            // See the program loaded in memory
            myComputer.dumpMemoryToJavaConsole();

            LoadStoreInstruction load = new LoadStoreInstruction((short) 0b0000010111010101, myComputer);
            load.print();

            ArithmeticLogicInstruction arr = new ArithmeticLogicInstruction((short) 0b0101000001000000, myComputer);
            arr.print();

            ShiftRotateInstruction shift = new ShiftRotateInstruction((short) 0b0111110100001111, myComputer);
            shift.print();

            InputOutputInstruction inOut = new InputOutputInstruction((short) 0b1111110100111111, myComputer);
            inOut.print();

            // Put a load instruction at address 6
            myComputer.setWord(6 ,(short) 0b0000011100011111);

            launchGUI();
            myComputer.powerOn();
//            myComputer.startExecutionLoop();
        } catch (Exception e) {
            System.out.println("Simulator crashed with " + e);
        }
    }

    static void launchGUI(){
        JFrame frame = new JFrame("Test");
        frame.setContentPane(new Test().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
