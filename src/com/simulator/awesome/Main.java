package com.simulator.awesome;

import javax.swing.*;
import java.io.File;
import java.lang.String;
import java.util.Arrays;

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
                launchGUI(myComputer);
            } else {
                // Not yet implemented, but saving this logic to run the simulator "headless"
                Assembler assembler1 = new Assembler();
                Assembler assembler2 = new Assembler();
                myComputer.powerOn((short) 100);
                myComputer.startExecutionLoop();

                // Pre-fill some data into the computer to be used by the demo assembly program
                String basePath = new File("").getAbsolutePath(); //get current base directory
                assembler1.loadFile(basePath.concat("/static/pre-fill-data-for-demo.txt"));
                myComputer.loadProgram(assembler1.input_arr, (short) 6);

                // Load in the load/store demonstration program
                assembler2.loadFile(basePath.concat("/static/demo-program.txt"));
                myComputer.loadProgram(assembler2.convertToMachineCode(), (short) 100);
            }

        } catch (Exception e) {
            System.out.println("Simulator crashed with " + e);
        }
    }

    static void launchGUI(Simulator context){
        JFrame frame = new JFrame("Test");
        frame.setContentPane(new Test(context).rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
