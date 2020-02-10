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
        put("HLT","000000");
        put("LDR","000001");
        put("STR","000010");
        put("LDA","000011");
        put("AMR","000100");
        put("SMR","000101");
        put("AIR","000110");
        put("SIR","000111");
        put("JZ","001010");
        put("JNE","001011");
        put("JCC","001100");
        put("JMA","001101");
        put("JSR","001110");
        put("RFS","001111");
        put("SOB","010000");
        put("JGE","010001");
        put("MLT","010100");
        put("DVD","010101");
        put("TRR","010110");
        put("AND","010111");
        put("ORR","011000");
        put("NOT","011001");
        put("TRAP","011110");
        put("SRC","011111");
        put("RRC","100000");
        put("FADD","100001");
        put("FSUB","100010");
        put("VADD","100011");
        put("VSUB","100100");
        put("CNVRT","100101");
        put("LDX","101001");
        put("STX","101010");
        put("LDFR","110010");
        put("STFR","110011");
        put("IN","111101");
        put("OUT","111110");
        put("CHK","111111");
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
            case "LDR":
            case "STR":
            case "LDA":
                mc_r = "00".substring(instructionParams[0].length()) + instructionParams[0]; // add left padding 0s so that the parameter is 2 bits
                mc_ix = "00".substring(instructionParams[1].length()) + instructionParams[1]; // add left padding 0s so that the parameter is 2 bits
                mc_address = "00000".substring(instructionParams[2].length()) + instructionParams[2]; // add left padding 0s so that the parameter is 5 bits
                mc_i = instructionParamsLength > 3 ? instructionParams[3]: "0"; //if the Indirect bit is set, include it. Otherwise, default to 0.
                machineCode = mc_opcode + mc_r + mc_ix + mc_i + mc_address;
                break;
            case "LDX":
            case "STX":
                // For both index registers, add left padding 0s so that the parameter is 2 bits
                String mc_x0 = "00".substring(instructionParams[0].length()) + instructionParams[0]; // the source or destination register to be loaded or stored, where x0 = x1..x3
                String mc_x1 = "00".substring(instructionParams[1].length()) + instructionParams[1]; // the indexing register to be used for calculating the effective address
                mc_address = "00000".substring(instructionParams[2].length()) + instructionParams[2]; // add left padding 0s so that the parameter is 5 bits
                mc_i = instructionParamsLength > 3 ? instructionParams[3] : "0"; //if the Indirect bit is set, include it. Otherwise, default to 0.
                machineCode = mc_opcode + mc_x0 + mc_x1 + mc_i + mc_address;
                break;
            case "HLT":
            case "AMR":
            case "SMR":
            case "AIR":
            case "SIR":
            case "JZ":
            case "JNE":
            case "JCC":
            case "JMA":
            case "JSR":
            case "RFS":
            case "SOB":
            case "JGE":
            case "MLT":
            case "DVD":
            case "TRR":
            case "AND":
            case "ORR":
            case "NOT":
            case "SRC":
            case "RRC":
            case "FADD":
            case "FSUB":
            case "VADD":
            case "TRAP":
            case "VSUB":
            case "CNVRT":
            case "LDFR":
            case "STFR":
            case "IN":
            case "OUT":
            case "CHK":
                break;
        }
        return machineCode;
    }
}