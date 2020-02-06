package com.simulator.awesome;

import javax.swing.*;
import java.io.File;
import java.lang.String;

class Main {
    public static void main(String[] args) {
        try {
            Simulator myComputer = new Simulator(2048);

            // See the program loaded in memory
            boolean isInteractive = true;
            boolean isDebug = false;

            if (isDebug) {
                myComputer.dumpMemoryToJavaConsole();
            }

            if (isInteractive) {
                JFrame frame = new JFrame("CSCI 6461 Computer Simulator - Yellow Team");
                frame.setContentPane(new Interface(myComputer).rootPanel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            } else {
                // Not yet implemented, but saving this logic to run the simulator "headless"
                Assembler assembler1 = new Assembler();
                Assembler assembler2 = new Assembler();

                // Pre-fill some data into the computer to be used by the demo assembly program
                String basePath = new File("").getAbsolutePath(); //get current base directory
                assembler1.loadFile(basePath.concat("/static/pre-fill-data-for-demo.txt"));
                myComputer.loadProgram(assembler1.input_arr, (short) 6);

                // Load in the load/store demonstration program
                assembler2.loadFile(basePath.concat("/static/demo-program.txt"));
                myComputer.loadProgram(assembler2.convertToMachineCode(), (short) 100);

                // IPL and Start the Execution Loop
                myComputer.powerOn((short) 100);
                myComputer.startExecutionLoop();
            }

        } catch (Exception e) {
            System.out.println("Simulator crashed with " + e);
        }
    }

}
