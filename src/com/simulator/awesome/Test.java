package com.simulator.awesome;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Test {
    private JTextField Input;
    public JPanel rootPanel;
    private JLabel inputLabel;
    private JButton haltButton;
    private JButton startButton;
    private JButton endButton;
    private JButton SSButton;
    private JButton LRButton;
    private JLabel R0Label;
    private JTextField R0TextField;
    private JTextField R1TextField;
    private JTextField R2TextField;
    private JLabel R1Label;
    private JTextField R3TextField;
    private JTextField IRTextField;
    private JLabel X1Label;
    private JTextField X1TextField;
    private JLabel X2Label;
    private JTextField X2TestField;
    private JTextField X3TestField;
    private JTextField MARTextField;
    private JTextField MBRTextField;
    private JTextField PCTextField;
    private JButton runButton;
    private JButton readButton;
    private JComboBox comboBox1;
    private JButton saveValueButton;
    private JLabel X3Label;
    private JLabel R2Label;
    private JLabel R3Label;
    private JLabel MARLabel;
    private JLabel PCLabel;
    private JLabel MBRLabel;
    private JLabel IRLabel;
    private JTextField textField1;
    private JTextField textField2;
    private Simulator context;

    public void refresh(){
        this.R0TextField.setText(Simulator.wordToString(this.context.getGeneralRegister((short)0)));
    }

    public Test(Simulator context) {
        this.context = context;
        this.refresh();

        // START - IPLs the Simulator
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.powerOn((short) 100);
            }
        });

        // RUN - Starts the Execution Loop
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.startExecutionLoop();
            }
        });

        // HALT - Pauses the Execution Loop
        haltButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (context.isRunning()){
                    context.pauseExecutionLoop();
                } else {
                    System.out.println("Cannot Pause! System is not Running!");
                }
            }
        });


        // END - Stops the Execution Loop and Clears the Memory and Registers
        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (context.isRunning()){
                    context.pauseExecutionLoop();
                }
                context.reset();
            }
        });

        // Single Step - Advanced forward one step. This is roughly a "cycle"
        SSButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.singleStep();
            }
        });

        LRButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("TestLoad Register");     //A Test output for the listener method
            }
        });
        readButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("TestRead");     //A Test output for the listener method
            }
        });

    }
}
