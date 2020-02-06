package com.simulator.awesome;

import javax.swing.*;
import java.io.File;
import java.lang.String;
import java.util.Arrays;

class Main {
    public static void main(String[] args) {
        try {
            Assembler assembler1 = new Assembler();
            Assembler assembler2 = new Assembler();

            Simulator myComputer = new Simulator(2048);

            // Pre-fill some data into the computer to be used by the demo assembly program
            String basePath = new File("").getAbsolutePath(); //get current base directory
            assembler1.loadFile(basePath.concat("/static/pre-fill-data-for-demo.txt"));
            myComputer.loadProgram(assembler1.input_arr, (short) 6);

            // Load in the load/store demonstration program
            assembler2.loadFile(basePath.concat("/static/demo-program.txt"));
            myComputer.loadProgram(assembler2.convertToMachineCode(), (short) 100);

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

            launchGUI();

            myComputer.powerOn((short) 100);
            //myComputer.startExecutionLoop();
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
