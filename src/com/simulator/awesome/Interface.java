package com.simulator.awesome;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static com.simulator.awesome.Simulator.*;
import static com.simulator.awesome.Utils.stringToWord;
import static com.simulator.awesome.Utils.wordToString;

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
    private JButton loadInstructionDemoButton;
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
    private JTextArea fieldEngineerConsole;
    private JButton loadProgram1Button;
    private JTabbedPane tabbedPane1;
    private JLabel readyForInputLabel;
    private Simulator context;
    private File selectedFile;
    private Integer consolePrinterLineNumber = 0;

    // Initialize image icons for indicator light
    String basePath = new File("").getAbsolutePath(); //get current base directory
    ImageIcon redLight = new ImageIcon(basePath.concat("/static/lamp-red.png"));
    ImageIcon greenLight = new ImageIcon(basePath.concat("/static/lamp-green.png"));

    public void refresh(){
        // Refresh General Purpose Registers R0..R3
        this.R0TextField.setText(wordToString(this.context.getGeneralRegister((short)0)));
        this.R1TextField.setText(wordToString(this.context.getGeneralRegister((short)1)));
        this.R2TextField.setText(wordToString(this.context.getGeneralRegister((short)2)));
        this.R3TextField.setText(wordToString(this.context.getGeneralRegister((short)3)));

        // Refresh Index Registers X1..X3
        this.X1TextField.setText(wordToString(this.context.getIndexRegister((short)1)));
        this.X2TextField.setText(wordToString(this.context.getIndexRegister((short)2)));
        this.X3TextField.setText(wordToString(this.context.getIndexRegister((short)3)));

        // Refresh other registers and fields: PC, MAR, MBR, MFR (not implemented), IR, CC (not implemented)
        this.PCTextField.setText(this.context.pc.toString());
        this.MARTextField.setText(this.context.memory.mar.toString());
        this.MBRTextField.setText(wordToString(this.context.memory.getMemoryBufferRegister()));
        this.MFRTextField.setText(this.context.mfr.toString());
        this.IRTextField.setText(wordToString(this.context.cu.getInstructionRegister()));
        this.CCTextField.setText(this.context.cc.toString());
        pollIOStatus();
    }

    public void pollIOStatus(){
        // If there is a value in the output buffer, print it to the console printer and then clear the buffer
        if (!this.context.io.isOutputBufferNull((short) 1)) {
            short charCode = this.context.io.getFirstWordFromOutputBuffer((short) 1);
            char poppedChar = (char)charCode;
            this.consolePrinter.append(String.valueOf(poppedChar));
            // Automatically scroll the console to the bottom
            this.consolePrinter.setCaretPosition(consolePrinter.getDocument().getLength());
        }

        // If there a value in the engineer's console buffer, print it to the engineer's console and then clear the buffer
        if (!this.context.io.isEngineersConsoleBufferNull()) {
            this.fieldEngineerConsole.append(context.io.getFirstLineFromEngineersOutputBuffer());
            // Automatically scroll the console to the bottom
            this.fieldEngineerConsole.setCaretPosition(fieldEngineerConsole.getDocument().getLength());
        }

        // If the computer is ready for input, enable the console keyboard
        if(this.context.msr.isReadyForInput() && !consoleKeyboard.isEnabled()){
            setUIReadyForInput(true);
        }

        // If the computer is not running or waiting for input, disable the HALT button
        if(!this.context.msr.isRunning() && !this.context.msr.isReadyForInput()){
            haltButton.setEnabled(false);
        }

        // If the computer is running or is waiting for input, disable the run button
        if(this.context.msr.isRunning() || this.context.msr.isReadyForInput()){
            runButton.setEnabled(false);
            runButton.setText("RUNNING");
        } else {
            runButton.setEnabled(true);
            runButton.setText("RUN");
        }
    }

    public void setUIReadyForInput(boolean state){
        if (state==true){
            consoleKeyboard.setEnabled(true);
            consoleKeyboard.setEditable(true);
            consoleKeyboard.requestFocus();
            returnButton.setEnabled(true);
            readyForInputLabel.setIcon(greenLight);
            readyForInputLabel.setText("Ready for Input");
            this.context.io.engineerConsolePrintLn("Waiting for user input.");
        } else {
            consoleKeyboard.setText("");
            consoleKeyboard.setEnabled(false);
            consoleKeyboard.setEditable(false);
            returnButton.setEnabled(false);
            readyForInputLabel.setIcon(redLight);
            readyForInputLabel.setText("Not Ready for Input");
        }
    }

    public Interface(Simulator context) {
        this.context = context;
        setUIReadyForInput(false);
        this.refresh();

        // START - IPLs the Simulator
        iplButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Pushing the IPL button loads the based addresses and ascii tables, ready for a user program to be loaded
                Assembler assembler1 = new Assembler();
                Assembler assembler2 = new Assembler();

                // Load Base Addresses representing every 32nd address from as a DataSet
                // Place it at the topmost addresses
                String basePath = new File("").getAbsolutePath(); //get current base directory
                assembler1.loadFile(basePath.concat("/static/base-addresses.txt"));

                short baseAddressTableLocation = context.loadProgram(assembler1.input_arr, (short) -1, true);
                // Assign Indirect to this dataset to address 30;
                context.memory.store((short)30, baseAddressTableLocation);

                // Load the ASCII table
                assembler2.loadFile(basePath.concat("/static/ascii.txt"));
                short asciiTableLocation = context.loadProgram(assembler2.input_arr, (short) -129, true);
                context.memory.store((short)29, asciiTableLocation);

                // Load the print-int subroutine
                assembler2.loadFile(basePath.concat("/static/print-int.txt"));
                assembler2.convertToMachineCode();
                short printIntLocation = context.loadProgram(assembler2.output_arr, (short) -330, false);
                context.memory.store((short) 28, printIntLocation);

                refresh();
            }
        });

        // RUN - Starts the Execution Loop
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.cu.startExecutionLoop();
                // Enable the halt button
                haltButton.setEnabled(true);
                refresh();
            }
        });

        // HALT - Pauses the Execution Loop
        haltButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (context.msr.isRunning() || context.msr.isReadyForInput()){
                    context.cu.pauseExecutionLoop();
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
                            context.setGeneralRegister((short) 0, stringToWord(input));
                            break;
                        case "R1":
                            context.setGeneralRegister((short) 1, stringToWord(input));
                            break;
                        case "R2":
                            context.setGeneralRegister((short) 2, stringToWord(input));
                            break;
                        case "R3":
                            context.setGeneralRegister((short) 3, stringToWord(input));
                            break;
                        case "X1":
                            context.setIndexRegister((short) 1, stringToWord(input));
                            break;
                        case "X2":
                            context.setIndexRegister((short) 2, stringToWord(input));
                            break;
                        case "X3":
                            context.setIndexRegister((short) 3, stringToWord(input));
                            break;
                        case "PC":
                            context.pc.set(stringToWord(input));
                            break;
                        case "MAR":
                            context.memory.mar.set(stringToWord(input));
                            break;
                        case "MBR":
                            context.memory.setMemoryBufferRegister(stringToWord(input));
                            break;
                        case "MFR":
                            // not implemented
                            break;
                        case "IR":
                            context.cu.setInstructionRegister(stringToWord(input));
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
                // Pause execution
                if (context.msr.isRunning()){
                    context.cu.pauseExecutionLoop();
                }
                // Reset the computer
                context.reset();
                // Clear the console printers
                consolePrinter.setText("");
                fieldEngineerConsole.setText("");
                // Reset the console printer line number counter
                consolePrinterLineNumber = 0;
                refresh();
            }
        });

        // Single Step - Advanced forward one step. This is roughly a "cycle"
        SSButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                context.cu.singleStep();
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
                    if (memoryLoc > 31 && memoryLoc < context.memory.getWordCount()-1) {
                        // Check that a file type has been selected.
                        if (fileTypeComboBox.getSelectedItem().toString().length() > 0) {
                            switch (fileTypeComboBox.getSelectedItem().toString()) {
                                case "Select File Type":
                                    JOptionPane.showMessageDialog(rootPanel, "ERROR: Please select the file type from the dropdown menu.");
                                    break;
                                case "Binary":
                                    // Load binary file into memory at the specified location
                                    Assembler assembler1 = new Assembler();
                                    context.loadProgram(assembler1.input_arr, memoryLoc, false);

                                    // Set the PC to the first program instruction
                                    context.powerOn(memoryLoc);
                                    refresh();
                                    break;
                                case "Assembly":
                                    // Convert assembly file to binary and load it into memory at the specified location
                                    Assembler assembler2 = new Assembler();
                                    assembler2.loadFile(selectedFile.getAbsolutePath());
                                    context.loadProgram(assembler2.convertToMachineCode(), memoryLoc, false);

                                    // Set the PC to the first program instruction
                                    context.powerOn(memoryLoc);
                                    refresh();
                                    break;
                            }
                        }
                    } else {
                        // Error: Invalid memory location
                        String maxLoc = Integer.toString(context.memory.getWordCount()-1);
                        JOptionPane.showMessageDialog(rootPanel, "ERROR: Memory location to insert the program must be within the valid range for unprotected memory (Minimum: "+32+") (Maximum: "+maxLoc+").");
                    }
                } else {
                    // Error: No file selected.
                    JOptionPane.showMessageDialog(rootPanel, "ERROR: Use the Choose File button to select a text file containing a binary or assembly program.");
                }
                refresh();
            }
        });

        loadInstructionDemoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Not yet implemented, but saving this logic to run the simulator "headless"
                Assembler assembler1 = new Assembler();

                // Load in the instruction demonstration program
                assembler1.loadFile(basePath.concat("/static/demo-program.txt"));
                short programLocation = context.loadProgram(assembler1.convertToMachineCode(), (short) 100, false);

                // Assign Indirect to this dataset to address 16;
                context.memory.store((short) 16, programLocation);

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
                    // Get the entered text and store it in the input buffer
                    context.io.addWordToInputBuffer((short) 0, stringToWord(Integer.toBinaryString(Integer.parseInt(consoleKeyboard.getText()))));
                    // Change input waiting state
                    context.msr.setReadyForInput(false);
                    // Disable input from the UI
                    setUIReadyForInput(false);
                    // Continue execution
                    context.cu.startExecutionLoop();
                } else {
                    JOptionPane.showMessageDialog(rootPanel, "ERROR: Input must only contain ASCII characters.");
                }
            }
        });
        loadProgram1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Reset the simulation and call IPL to ensure a clean slate
                resetButton.doClick();
                iplButton.doClick();

                // Not yet implemented, but saving this logic to run the simulator "headless"
                Assembler assembler1 = new Assembler();

                // Assemble the program and load it into the computer at memory location 101.
                String basePath = new File("").getAbsolutePath(); //get current base directory
                assembler1.loadFile(basePath.concat("/static/program-one.txt"));
                short programLocation = context.loadProgram(assembler1.convertToMachineCode(), (short) 101, false);

                // Assign Indirect to this dataset to address 16;
                context.memory.store((short) 16, programLocation);

                // IPL and Start the Execution Loop
                context.powerOn((short) 101);
                refresh();
            }
        });
        // Allow pressing the ENTER key on the keyboard to input data
        consoleKeyboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnButton.doClick();
            }
        });
    }
}
