package com.simulator.awesome;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Interface {
    private JTextField withValueInput;
    public JPanel rootPanel;
    private JLabel modifyRegisterLabel;
    private JButton haltButton;
    private JButton iplButton;
    private JButton resetButton;
    private JButton SSButton;
    private JButton loadProgramButton;
    private JTextField R0TextField;
    private JTextField R1TextField;
    private JTextField R2TextField;
    private JTextField R3TextField;
    private JTextField IRTextField;
    private JLabel R1Label;
    private JTextField X1TextField;
    private JTextField X2TextField;
    private JTextField X3TextField;
    private JTextField MARTextField;
    private JTextField MBRTextField;
    private JTextField PCTextField;
    private JButton runButton;
    private JButton chooseFileButton;
    private JComboBox registerComboBox;
    private JButton saveValueButton;
    private JLabel R2Label;
    private JLabel R3Label;
    private JLabel MARLabel;
    private JLabel PCLabel;
    private JLabel MBRLabel;
    private JTextField CCTextField;
    private JTextField MFRTextField;
    private JLabel withValueLabel;
    private JLabel MFRLabel;
    private JLabel CCLabel;
    private JButton loadLoadStoreDemoButton;
    private JComboBox fileTypeComboBox;
    private JLabel R0Label;
    private JLabel IRLabel;
    private JLabel X1Label;
    private JLabel X2Label;
    private JLabel X3Label;
    private JLabel selectedFileLabel;
    private JSpinner programMemoryLocSpinner;
    private JButton returnButton;
    private JTextArea consolePrinter;
    private JFormattedTextField consoleKeyboard;
    private Simulator context;
    private File selectedFile;

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
        this.PCTextField.setText(Simulator.wordToString(this.context.getProgramCounter()).substring(3,15)); //only 12 bits
        this.MARTextField.setText(Simulator.wordToString(this.context.getMemoryAddressRegister()).substring(3,15)); //only 12 bits
        this.MBRTextField.setText(Simulator.wordToString(this.context.getMemoryBufferRegister()));
        this.MFRTextField.setText(Simulator.wordToString(this.context.getMachineFaultRegister()).substring(11,15)); // only 4 bits
        this.IRTextField.setText(Simulator.wordToString(this.context.getInstructionRegister()));
        this.CCTextField.setText(Simulator.wordToString(this.context.getConditionCode()).substring(11,15)); // only 4 bits

        // If there is a value in the output buffer, print it to the console printer and then clear the buffer
        if (!this.context.isOutputBufferNull((short) 1)) {
                this.consolePrinter.append(Simulator.wordToString(this.context.getFirstWordFromOutputBuffer((short) 1))+"\n");
        }
    }

    public Interface(Simulator context) {
        this.context = context;
        this.refresh();

        // START - IPLs the Simulator
        iplButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Pushing the IPL button loads the demonstration program, ready for the grader to press Single Step
                loadLoadStoreDemoButton.doClick();
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
                    // Error: Halt button pressed but system is not running.
                    JOptionPane.showMessageDialog(rootPanel, "ERROR: Cannot Pause! System is not running.");
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
                    // Error: Input value was not a 16-bit binary number
                    JOptionPane.showMessageDialog(rootPanel, "ERROR: Input value must be a 16-bit binary number.");
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

        // Load Program - Allows loading a program (in binary or assembly) from a text file.
        loadProgramButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check that a file is selected
                if(selectedFile != null && selectedFile.isFile()){
                    short memoryLoc = (short) Integer.parseInt(programMemoryLocSpinner.getValue().toString());
                    // Check that the desired memory location is within the valid range.
                    if (memoryLoc > 5 && memoryLoc < context.getWordCount()-1) {
                        // Check that a file type has been selected.
                        if (fileTypeComboBox.getSelectedItem().toString().length() > 0) {
                            switch (fileTypeComboBox.getSelectedItem().toString()) {
                                case "Select File Type":
                                    JOptionPane.showMessageDialog(rootPanel, "ERROR: Please select the file type from the dropdown menu.");
                                    break;
                                case "Binary":
                                    // Load binary file into memory at the specified location
                                    Assembler assembler1 = new Assembler();
                                    context.loadProgram(assembler1.input_arr, memoryLoc);

                                    // Set the PC to the first program instruction
                                    context.powerOn(memoryLoc);
                                    refresh();
                                    break;
                                case "Assembly":
                                    // Convert assembly file to binary and load it into memory at the specified location
                                    Assembler assembler2 = new Assembler();
                                    assembler2.loadFile(selectedFile.getAbsolutePath());
                                    context.loadProgram(assembler2.convertToMachineCode(), memoryLoc);

                                    // Set the PC to the first program instruction
                                    context.powerOn(memoryLoc);
                                    refresh();
                                    break;
                            }
                        }
                    } else {
                        // Error: Invalid memory location
                        String maxLoc = Integer.toString(context.getWordCount()-1);
                        JOptionPane.showMessageDialog(rootPanel, "ERROR: Memory location to insert the program must be within the valid range for unprotected memory (Minimum: "+6+") (Maximum: "+maxLoc+").");
                    }
                } else {
                    // Error: No file selected.
                    JOptionPane.showMessageDialog(rootPanel, "ERROR: Use the Choose File button to select a text file containing a binary or assembly program.");
                }
                refresh();
            }
        });

        loadLoadStoreDemoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Not yet implemented, but saving this logic to run the simulator "headless"
                Assembler assembler1 = new Assembler();
                Assembler assembler2 = new Assembler();

                // Pre-fill some data into the computer to be used by the demo assembly program
                String basePath = new File("").getAbsolutePath(); //get current base directory
                assembler1.loadFile(basePath.concat("/static/pre-fill-data-for-demo.txt"));
                context.loadProgram(assembler1.input_arr, (short) 6);

                // Load in the load/store demonstration program
                assembler2.loadFile(basePath.concat("/static/demo-program.txt"));
                context.loadProgram(assembler2.convertToMachineCode(), (short) 100);

                // IPL and Start the Execution Loop
                context.powerOn((short) 100);
                refresh();
            }
        });
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Launch a file chooser
                JFileChooser chooser = new JFileChooser();

                // Only allow selection of text files
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        ".txt", "txt");
                chooser.setFileFilter(filter);

                // Store the selected file location and update the interface.
                int returnVal = chooser.showOpenDialog(rootPanel);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    selectedFileLabel.setText(chooser.getSelectedFile().getName());
                    selectedFile = chooser.getSelectedFile();
                }
                refresh();
            }
        });
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Validate that the text entered contains one or more ASCII value
                if(consoleKeyboard.getText().matches("[\\x00-\\x7F]+")){
                    // TODO: read the input using the IN function
                    // Can we only read one character at a time?
                    // How do we pause execution while waiting for user input?
                    // We probably need better input validation than this
                    // context.setInputBuffer((short) 1, Simulator.stringToWord(consoleKeyboard.getText()));
                } else {
                    JOptionPane.showMessageDialog(rootPanel, "ERROR: Input must only contain ASCII characters.");
                }
            }
        });
    }
}
