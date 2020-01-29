package com.simulator.awesome;

public class Simulator {

    // The number of 16-bit words we have in memory
    int word_count;

    // The linear memory of our simulated system.
    // Shorts in Java are 16-bit, so this is word addressable
    // The index represents the nth word, zero indexed
    public Short memory[];

    // Program Counter: Address of the next instruction to be executed
    // Uses the least significant 12 bits
    short pc;
    static final short PC_MASK = (short)0b0000111111111111;

    // Condition code.
    // Uses the least significant 4 bytes
    // set when arithmetic/logical operations are executed
    // it has four 1-bit elements: overflow, underflow, division by zero, equal-or-not.
    // They may be referenced as cc(0), cc(1), cc(2), cc(3).
    byte cc;
    // Or by the names OVERFLOW, UNDERFLOW, DIVZERO, EQUALORNOT
    // TODO: Determine if I have this flipped or not ~SPM
    static final byte OVERFLOW =   (byte)0b1000;
    static final byte UNDERFLOW =  (byte)0b0100;
    static final byte DIVZERO =    (byte)0b0010;
    static final byte EQUALORNOT = (byte)0b0001;

    // Instruction register. Holds the instruction to be executed
    short ir;

    // Memory Address Register. Holds the address of the word to be fetched from memory
    short mar;
    static final short MAR_MASK = (short)0b0000111111111111;

    // Memory Buffer Register.
    // Holds the word just fetched or the word to be stored into memory.
    // If this is a word we're writing to memory, it can either be something to be written
    // or something that we've already written, ya dig?
    short mbr;

    // Machine Fault Register.
    // Contains the ID code of a machine fault after it occurs
    byte mfr;

    static final byte MFR_MASK = (byte)0b00001111;

    // The four general purpose registers
    short r0, r1, r2, r3;

    // The three index registers
    short x1, x2, x3;

    // TODO: What are the mystery registers we're missing?

    Simulator(int word_count) {
        // Allocate Linear Memory
        this.word_count = 2048;
        this.memory = new Short[2048];

        // Allocate and zero out all Registers
        this.pc = 0;
        this.cc = 0;
        this.ir = 0;
        this.mar = 0;
        this.mbr = 0;
        this.mfr = 0;
        this.r0 = 0;
        this.r1 = 0;
        this.r2 = 0;
        this.r3 = 0;
        this.x1 = 0;
        this.x2 = 0;
        this.x3 = 0;
    }

    public static short extract_opcode(short word) {
        // OPCODE is 0-4bits, so right shift by 11
        // the >>> operator is a bitshift that includes the "sign bit"
        short opcode = (short)(word>>>11);
        return opcode;
    }

    public static void parse_and_execute(short word) {
        short opcode = Simulator.extract_opcode(word);

        switch(opcode) {
            case 0:
                System.out.println("HLT");
                break;
            case 1:
                System.out.println("LDR");
                break;
            case 2:
                System.out.println("STR");
                break;
            case 3:
                System.out.println("LDA");
                break;
            case 4:
                System.out.println("AMR");
                break;
            case 5:
                System.out.println("SMR");
                break;
            case 6:
                System.out.println("AIR");
                break;
            case 7:
                System.out.println("SIR");
                break;
            case 10:
                System.out.println("JZ");
                break;
            case 11:
                System.out.println("JNE");
                break;
            case 12:
                System.out.println("JCC");
                break;
            case 13:
                System.out.println("JMA");
                break;
            case 14:
                System.out.println("JSR");
                break;
            case 15:
                System.out.println("RFS");
                break;
            case 16:
                System.out.println("SOB");
                break;
            case 17:
                System.out.println("JFE");
                break;
            case 20:
                System.out.println("MLT");
                break;
            case 21:
                System.out.println("DVD");
                break;
            case 22:
                System.out.println("TRR");
                break;
            case 23:
                System.out.println("AND");
                break;
            case 24:
                System.out.println("ORR");
                break;
            case 25:
                System.out.println("NOT");
                break;
            case 31:
                System.out.println("SRC");
                break;
            case 32:
                System.out.println("RRC");
                break;
            case 33:
                System.out.println("FADD");
                break;
            case 34:
                System.out.println("FSUB");
                break;
            case 35:
                System.out.println("VADD");
                break;
            // case 36:
            //   System.out.println("VSUB");
            //   break;
            case 36:
                System.out.println("TRAP");
                break;
            case 37:
                System.out.println("CNVRT");
                break;
            case 41:
                System.out.println("LDX");
                break;
            case 42:
                System.out.println("STX");
                break;
            case 50:
                System.out.println("LDFR");
                break;
            case 51:
                System.out.println("STFR");
                break;
            case 61:
                System.out.println("IN");
                break;
            case 62:
                System.out.println("OUT");
                break;
            case 63:
                System.out.println("CHK");
                break;
        }

    }

    // Given a 16-bit short, generates a binary string
    public static String word_to_string(short word){
        String binary_string = Integer.toBinaryString(Short.toUnsignedInt(word));
        return String.format("%1$16s", binary_string).replace(' ', '0');
    }
}
