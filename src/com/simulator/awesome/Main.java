package com.simulator.awesome;

import javax.swing.*;
import java.io.File;
import java.lang.String;

class Main {
    public static void main(String[] args) {
        try {
            boolean isInteractive = true;
            boolean isDebug = false;

            Simulator myComputer = new Simulator(2048);

            if (isDebug) {
                myComputer.memory.dump();
            }

            if (isInteractive) {
                myComputer.attachConsole();
                myComputer.io.initializeIOBuffers();
                Interface myInterface = new Interface(myComputer);
                JFrame frame = new JFrame("CSCI 6461 Computer Simulator - Yellow Team");
                frame.setContentPane(myInterface.rootPanel);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
                while(isInteractive){
                    myInterface.pollIOStatus();
                }
            } else {
                System.out.println("Headless non-interactive operation is not yet implemented.");
            }

        } catch (Exception e) {
            System.out.println("Simulator crashed with " + e);
        }
    }

}
