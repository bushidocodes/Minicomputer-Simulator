package com.simulator.awesome;

import javax.swing.JFrame;

class Main {
    public static void main(String[] args) {
        try {
            Simulator myComputer = new Simulator(2048);
            myComputer.attachConsole();
            myComputer.io.initializeIOBuffers();

            Interface myInterface = new Interface(myComputer);
            JFrame frame = new JFrame("CSCI 6461 Computer Simulator - Yellow Team");
            frame.setContentPane(myInterface.rootPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);

            while (true) {
                myInterface.pollIOStatus();
            }
        } catch (Exception e) {
            System.err.println("Simulator crashed:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
