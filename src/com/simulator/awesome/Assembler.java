package com.simulator.awesome;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Assembler {
    // Define the opcodes for each instruction
    Map<String, String> opcodeMap = new HashMap<String, String>() {{
        put("HLT", "000000");
        put("LDR", "000001");
        put("STR", "000010");
        put("LDA", "000011");
        put("AMR", "000100");
        put("SMR", "000101");
        put("AIR", "000110");
        put("SIR", "000111");
        put("JZ", "001000");
        put("JNE", "001001");
        put("JCC", "001010");
        put("JMA", "001011");
        put("JSR", "001100");
        put("RFS", "001101");
        put("SOB", "001110");
        put("JGE", "001111");
        put("MLT", "010000");
        put("DVD", "010001");
        put("TRR", "010010");
        put("AND", "010011");
        put("ORR", "010100");
        put("NOT", "010101");
        put("SRC", "011001");
        put("RRC", "011010");
        put("FADD", "011011");
        put("FSUB", "011100");
        put("VADD", "011101");
        put("TRAP", "011110");
        //put("VSUB", "011110"); what is the correct opcode for VSUB?
        put("CNVRT", "011111");
        put("LDX", "100001");
        put("STX", "100010");
        put("LDFR", "101000");
        put("STFR", "101001");
        put("IN", "110001");
        put("OUT", "110010");
        put("CHK", "110011");
    }};

    // Reads a text file line-by-line and converts assembly code into machine code.
    String[] input_arr;
    String[] output_arr;

    public void loadFile(String filepath) {
        // Count the number of lines in the file, and then allocate the same amount of space in an array in order to store it.
        try {
            long lineCount = Files.lines(Paths.get(filepath)).count();
            input_arr = new String[Math.toIntExact(lineCount)];
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read the assembly code from the text file and then store each line in an array for processing.
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filepath));
            String currentLine = reader.readLine();
            int lineCounter = 0;
            // Iterate through the text file line-by-line, storing each line in the array.
            while (lineCounter < input_arr.length) {
                input_arr[lineCounter] = currentLine;
                currentLine = reader.readLine();
                lineCounter++;
            }
            reader.close();
            System.out.println("Loaded an assembly file containing "+input_arr.length+" lines.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] convertToMachineCode(){
        // Allocate space for the output array.
        output_arr = new String[input_arr.length];
        for(int lineCounter = 0; lineCounter < input_arr.length; lineCounter++){
            String currLine = input_arr[lineCounter];
            // Strip out the comments, if any
            String strippedInstructions = currLine.split(";")[0];

            // Get the instruction
            String instruction[] = strippedInstructions.split(" ");

            // Get the parameters
            String[] instructionParams = instruction[1].split(",");

            // Convert the data to machine code and save in the output array
            output_arr[lineCounter] = processInstruction(instruction[0],instructionParams);
        }
        return output_arr;
    }

    private String processInstruction(String instruction, String[] instructionParams) {
        int instructionParamsLength = instructionParams.length;
        String mc_opcode = opcodeMap.get(instruction);
        String mc_r = "";
        String mc_ix = "";
        String mc_i = "";
        String mc_address = "";
        String machineCode = "";

        // Convert the parameters to binary
        for (int i = 0; i<instructionParamsLength; i++){
            instructionParams[i] = Integer.toBinaryString(Integer.parseInt(instructionParams[i]));
        }

        switch (instruction){
            case "HLT":
                break;
            case "LDR":
            case "STR":
            case "LDA":
                mc_r = "00".substring(instructionParams[0].length()) + instructionParams[0]; // add left padding 0s so that the parameter is 2 bits
                mc_ix = "00".substring(instructionParams[1].length()) + instructionParams[1]; // add left padding 0s so that the parameter is 2 bits
                mc_address = "00000".substring(instructionParams[2].length()) + instructionParams[2]; // add left padding 0s so that the parameter is 5 bits
                if (instructionParamsLength>3){
                    mc_i = instructionParams[3]; //if the Indirect bit is set, include it. Otherwise, default to 0.
                } else {
                    mc_i = "0";
                }
                machineCode = mc_opcode + mc_r + mc_ix + mc_i + mc_address;
                break;
            case "LDX":
            case "STX":
                mc_r = "00"; // r is assumed to be zero for index register load and store
                mc_ix = "00".substring(instructionParams[0].length()) + instructionParams[0]; // add left padding 0s so that the parameter is 2 bits
                mc_address = "00000".substring(instructionParams[1].length()) + instructionParams[1]; // add left padding 0s so that the parameter is 5 bits
                if (instructionParamsLength>2){
                    mc_i = instructionParams[2]; //if the Indirect bit is set, include it. Otherwise, default to 0.
                } else {
                    mc_i = "0";
                }
                machineCode = mc_opcode + mc_r + mc_ix + mc_i + mc_address;
                break;
            case "AMR":
                break;
            case "SMR":
                break;
            case "AIR":
                break;
            case "SIR":
                break;
            case "JZ":
                break;
            case "JNE":
                break;
            case "JCC":
                break;
            case "JMA":
                break;
            case "JSR":
                break;
            case "RFS":
                break;
            case "SOB":
                break;
            case "JGE":
                break;
            case "MLT":
                break;
            case "DVD":
                break;
            case "TRR":
                break;
            case "AND":
                break;
            case "ORR":
                break;
            case "NOT":
                break;
            case "SRC":
                break;
            case "RRC":
                break;
            case "FADD":
                break;
            case "FSUB":
                break;
            case "VADD":
                break;
            case "TRAP":
                break;
            case "VSUB":
                break;
            case "CNVRT":
                break;
            case "LDFR":
                break;
            case "STFR":
                break;
            case "IN":
                break;
            case "OUT":
                break;
            case "CHK":
                break;
        }
        return machineCode;
    }
}