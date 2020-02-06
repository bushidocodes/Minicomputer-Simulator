package com.simulator.awesome;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Interface {
    private JTextField withValueInput;
    public JPanel rootPanel;
    private JLabel modifyRegisterLabel;
    private JButton haltButton;
    private JButton iplButton;
    private JButton resetButton;
    private JButton SSButton;
    private JButton loadAssemblyButton;
    private JLabel R0Label;
    private JTextField R0TextField;
    private JTextField R1TextField;
    private JTextField R2TextField;
    private JLabel X1Label;
    private JTextField R3TextField;
    private JTextField IRTextField;
    private JLabel R1Label;
    private JTextField X1TextField;
    private JLabel X2Label;
    private JTextField X2TextField;
    private JTextField X3TextField;
    private JTextField MARTextField;
    private JTextField MBRTextField;
    private JTextField PCTextField;
    private JButton runButton;
    private JButton loadBinaryButton;
    private JComboBox registerComboBox;
    private JButton saveValueButton;
    private JLabel X3Label;
    private JLabel R2Label;
    private JLabel R3Label;
    private JLabel MARLabel;
    private JLabel PCLabel;
    private JLabel MBRLabel;
    private JLabel IRLabel;
    private JTextField CCTextField;
    private JTextField MFRTextField;
    private JLabel withValueLabel;
    private JLabel MFRLabel;
    private JLabel CCLabel;
    private Simulator context;
    private boolean isLooping;

    public void refresh(){
        // Refresh General Purpose Registers R0..R3
        this.R0TextField.setText(Simulator.wordToString(this.context.getGeneralRegister((short)0)));
        this.R1TextField.setText(Simulator.wordToString(this.context.getGeneralRegister((short)1)));
        this.R2TextField.setText(Simulator.wordToString(this.context.getGeneralRegister((short)2)));
        this.R3TextField.setText(Simulator.wordToString(this.context.getGeneralRegister((short)3)));

        // Refresh Index Registers X1..X3
        this.X1TextField.setText(Simulator.wordToString(this.context.getIndexRegister((short)1)));
        this.X2TextField.setText(Simulator.wordToString(this.context.getIndexRegister((short)2)));
        this.X3TextField.setText(Simulator.wordToString(this.context.getIndexRegister((short)3)));

        // Refresh other registers and fields: PC, MAR, MBR, MFR (not implemented), IR, CC (not implemented)
        this.PCTextField.setText(Simulator.wordToString(this.context.getProgramCounter()));
        this.MARTextField.setText(Simulator.wordToString(this.context.getMemoryAddressRegister()));
        this.MBRTextField.setText(Simulator.wordToString(this.context.getMemoryBufferRegister()));
        //this.MFRTextField.setText(Simulator.wordToString(this.context.getMachineFaultRegister()));
        this.IRTextField.setText(Simulator.wordToString(this.context.getInstructionRegister()));
        //this.CCTextField.setText(Simulator.wordToString(this.context.getConditionCode())));
    }

    public Interface(Simulator context) {
        this.context = context;
        this.refresh();

        // START - IPLs the Simulator
        iplButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.powerOn((short) 100);
                refresh();
            }
        });

        // RUN - Starts the Execution Loop
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.startExecutionLoop();
                refresh();
            }
        });

        // HALT - Pauses the Execution Loop
        haltButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (context.isRunning()){
                    context.pauseExecutionLoop();
                    refresh();
                } else {
                    System.out.println("Cannot Pause! System is not Running!");
                }
            }
        });

        // SAVE VALUE - Saves a value directly to a register from the interface
        saveValueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check that input string is 16 bits and only contains 0 or 1
                String input = withValueInput.getText();
                if(input.length() == 16 && input.matches("^[01]+$")){
                    switch(registerComboBox.getSelectedItem().toString()){
                        //PC, MAR, MBR, MFR (not implemented), IR, CC (not implemented)
                        case "R0":
                            context.setGeneralRegister((short) 0, context.stringToWord(input));
                            break;
                        case "R1":
                            context.setGeneralRegister((short) 1, context.stringToWord(input));
                            break;
                        case "R2":
                            context.setGeneralRegister((short) 2, context.stringToWord(input));
                            break;
                        case "R3":
                            context.setGeneralRegister((short) 3, context.stringToWord(input));
                            break;
                        case "X1":
                            context.setIndexRegister((short) 1, context.stringToWord(input));
                            break;
                        case "X2":
                            context.setIndexRegister((short) 2, context.stringToWord(input));
                            break;
                        case "X3":
                            context.setIndexRegister((short) 3, context.stringToWord(input));
                            break;
                        case "PC":
                            context.setProgramCounter(context.stringToWord(input));
                            break;
                        case "MAR":
                            context.setMemoryAddressRegister(context.stringToWord(input));
                            break;
                        case "MBR":
                            context.setMemoryBufferRegister(context.stringToWord(input));
                            break;
                        case "MFR":
                            // not implemented
                            break;
                        case "IR":
                            context.setInstructionRegister(context.stringToWord(input));
                            break;
                        case "CC":
                            // not implemented
                            break;
                    }
                } else {
                    withValueInput.setText("ERR: 16-bit binary only");
                }
                refresh();
            }
        });

        // END - Stops the Execution Loop and Clears the Memory and Registers
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (context.isRunning()){
                    context.pauseExecutionLoop();
                }
                context.reset();
                refresh();
            }
        });

        // Single Step - Advanced forward one step. This is roughly a "cycle"
        SSButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.singleStep();
                refresh();
            }
        });

        loadAssemblyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("TestLoad Register");     //A Test output for the listener method
                refresh();
            }
        });
        loadBinaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("TestRead");     //A Test output for the listener method
                refresh();
            }
        });

    }
}
