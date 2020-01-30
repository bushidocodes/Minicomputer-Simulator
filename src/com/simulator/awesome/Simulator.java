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

    /**
     OPCODE 00 - Halt the Machine
     HLT
    */
    public static void halt() {
        System.out.println("Halting...");
        System.exit(0);
    }

    /**
     OPCODE 01 - Load Register From Memory
     LDR r, x, address[,I]
     r = 0..3
     r <- c(EA)
     note that EA is computed as given above
    */
    public static void load_register_from_memory(){
        System.out.println("LDR");
    }

    /**
     OPCODE 02 - Store Register To Memory
     STR r, x, address[,I]
     r = 0..3
     Memory(EA) <- c(r)
    */
    public static void store_register_to_memory(){
        System.out.println("STR");
    }

    /**
     OPCODE 03 - Load Register with Address
     LDA r, x, address[,I]
     r = 0..3
     r <- EA
    */
    public static void load_register_with_address(){
        System.out.println("LDA");
    }

    /**
     OPCODE 04 - Add Memory To Register
     AMR r, x, address[,I]
     r = 0..3
     r<- c(r) + c(EA)
    */
    public static void add_memory_to_register(){
        System.out.println("AMR");
    }

    /**
     OPCODE 05 - Subtract Memory From Register
     SMR r, x, address[,I]
     r = 0..3
     r<- c(r) – c(EA)
    */
    public static void subtract_memory_from_register(){
        System.out.println("SMR");
    }


    /**
     * OPCODE 06 - Add Immediate to Register
     * AIR r, immed
     * r = 0..3
     * r <- c(r) + Immed
     * Note:
     *  1. if Immed = 0, does nothing
     *  2. if c(r) = 0, loads r with Immed
     *  IX and I are ignored in this instruction
     */
    public static void add_immediate_to_register(){
        System.out.println("AIR");
    }


    /**
     OPCODE 07 - Subtract Immediate from Register
     SIR r, immed
     r = 0..3
     r <- c(r) - Immed
     Note:
         1. if Immed = 0, does nothing
         2. if c(r) = 0, loads r1 with –(Immed)
         IX and I are ignored in this instruction
    */
    public static void subtract_immediate_from_register(){
        System.out.println("SIR");
    }


    /**
     OPCODE 10 - Jump If Zero
     JZ r, x, address[,I]
     If c(r) = 0, then PC <- EA
     Else PC <- PC+1
    */
    public static void jump_if_zero(){
        System.out.println("JZ");
    }

    /**
     OPCODE 11 - Jump If Not Equal
     JNE r, x, address[,I]
     If c(r) != 0, then PC <-- EA
     Else PC <- PC + 1
    */
    public static void jump_if_not_zero(){
        System.out.println("JZ");
    }

    /**
     * OPCODE 12 - Jump If Condition Code
     * JCC cc, x, address[,I]
     * cc replaces r for this instruction
     * cc takes values 0, 1, 2, 3 as above and specifies the bit in the Condition Code Register to check;
     * If cc bit  = 1, PC <- EA
     * Else PC <- PC + 1
     **/
    public static void jump_if_condition_code(){
        System.out.println("JCC");
    }

    /**
     * OPCODE 13 - Unconditional Jump To Address
     * JMA x, address[,I]
     * PC <- EA,
     * Note: r is ignored in this instruction
     */
    public static void unconditional_jump_to_address(){
        System.out.println("JMA");
    }

    /**
     OPCODE 14 - Jump and Save Return Address
     JSR x, address[,I]
     R3 <- PC+1;
     PC <- EA
     R0 should contain pointer to arguments
     Argument list should end with –1 (all 1s) value
    */
    public static void jump_and_save_return_address(){
        System.out.println("JSR");
    }

    /**
     OPCODE 15 - Return From Subroutine
     w/ return code as Immed portion (optional) stored in the instruction’s address field.
     RFS Immed
     R0 <- Immed; PC <- c(R3)
     IX, I fields are ignored.
    */
    public static void return_from_subroutine(){
        System.out.println("RFS");
    }

    /**
     OPCODE 16 - Subtract One and Branch.
     SOB r, x, address[,I]
     r = 0..3
     r <- c(r) – 1
     If c(r) > 0,  PC <- EA;
     Else PC <- PC + 1
    */
    public static void subtract_one_and_branch(){
        System.out.println("SOB");
    }

    /**
     OPCODE 17 - Jump Greater Than or Equal To
     JGE r,x, address[,I]
     If c(r) >= 0, then PC <- EA
     Else PC <- PC + 1
    */
    public static void jump_greater_than_or_equal_to(){
        System.out.println("JGE");
    }

    /**
     OPCODE 20 - Multiply Register by Register
     MLT rx,ry
     rx, rx+1 <- c(rx) * c(ry)
     rx must be 0 or 2
     ry must be 0 or 2
     rx contains the high order bits, rx+1 contains the low order bits of the result
     Set OVERFLOW flag, if overflow
    */
    public static void multiply_register_by_register(){
        System.out.println("MLT");
    }

    /**
     OPCODE 21 - Divide Register by Register
     DVD rx,ry
     rx, rx+1 <- c(rx)/ c(ry)
     rx must be 0 or 2
     rx contains the quotient; rx+1 contains the remainder
     ry must be 0 or 2
     If c(ry) = 0, set cc(3) to 1 (set DIVZERO flag)
    */
    public static void divide_register_by_register(){
        System.out.println("DVD");
    }

    /**
     OPCODE 22 - Test the Equality of Register and Register
     TRR rx, ry
     If c(rx) = c(ry), set cc(4) <- 1; else, cc(4) <- 0
    */
    public static void test_the_equality_of_register_and_register(){
        System.out.println("TRR");
    }

    /**
     OPCODE 23 - Logical And of Register and Register
     AND rx, ry
     c(rx) <- c(rx) AND c(ry)
    */
    public static void logical_and_of_register_and_register(){
        System.out.println("AND");
    }

    /**
     OPCODE 24 - Logical Or of Register and Register
     ORR rx, ry
     c(rx) <- c(rx) OR c(ry)
    */
    public static void logical_or_of_register_and_register(){
        System.out.println("ORR");
    }

    /**
     OPCODE 25 - Logical Not of Register To Register
     NOT rx
     C(rx) <- NOT c(rx)
    */
    public static void logical_not_of_register_and_register(){
        System.out.println("NOT");
    }


    /**
     OPCODE 31 - Shift Register by Count
     SRC r, count, L/R, A/L
     c(r) is shifted left (L/R =1) or right (L/R = 0) either logically (A/L = 1) or arithmetically (A/L = 0)
     XX, XXX are ignored
     Count = 0…15
     If Count = 0, no shift occurs
    */
    public static void shift_register_by_count(){
        System.out.println("SRC");
    }

    /**
     OPCODE 32 - Rotate Register by Count
     RRC r, count, L/R, A/L
     c(r) is rotated left (L/R = 1) or right (L/R =0) either logically (A/L =1)
     XX, XXX is ignored
     Count = 0…15
     If Count = 0, no rotate occurs
    */
    public static void rotate_register_by_count(){
        System.out.println("RRC");
    }

    /**
     OPCODE 33 - Floating Add Memory To Register
     FADD fr, x, address[,I]
     c(fr) <- c(fr) + c(EA)
     c(fr) <- c(fr) + c(c(EA)), if I bit set
     fr must be 0 or 1.
     OVERFLOW may be set
    */
    public static void floating_add_memory_to_register(){
        System.out.println("FADD");
    }

    /**
     OPCODE 34 - Floating Subtract Memory From Register
     FSUB fr, x, address[,I]
     c(fr) <- c(fr) - c(EA)
     c(fr) <- c(fr) - c(c(EA)), if I bit set
     fr must be 0 or 1
     UNDERFLOW may be set
    */
    public static void floating_subtract_memory_from_register(){
        System.out.println("FSUB");
    }

    /**
     OPCODE 35 - Vector Add
     VADD fr, x, address[,I]
     fr contains the length of the vectors
     c(EA) or c(c(EA)), if I bit set, is address of first vector
     c(EA+1) or c(c(EA+1)), if I bit set, is address of the second vector
     Let V1 be vector at address; Let V2 be vector at address+1
     Then, V1[i] = V1[i]+ V2[i], i = 1, c(fr).
    */
    public static void vector_add(){
        System.out.println("VADD");
    }

    /**
     OPCODE 36 - Vector Subtract
     VSUB fr, x, address[,I]
     fr contains the length of the vectors
     c(EA) or c(c(EA)), if I bit set is address of first vector
     c(EA+1) or c(c(EA+1)), if I bit set is address of the second vector
     Let V1 be vector at address; Let V2 be vector at address+1
     Then, V1[i] = V1[i] - V2[i], i = 1, c(fr).
    */
    public static void vector_substract(){
        System.out.println("VSUB");
    }

    /**
     * OPCODE 36 - Trap
     * Traps to memory address 0, which contains the address of a table in memory.
     * Stores the PC+1 in memory location 2.
     * The table can have a maximum of 16 entries representing 16 routines for user-specified instructions stored elsewhere in memory.
     * Trap code contains an index into the table, e.g. it takes values 0 – 15.
     * When a TRAP instruction is executed, it goes to the routine whose address is in memory location 0,
     * executes those instructions, and returns to the instruction stored in memory location 2.
     * The PC+1 of the TRAP instruction is stored in memory location 2.
     */
    public static void trap(){
        System.out.println("TRAP");
    }

    /**
     OPCODE 37 - Convert to Fixed/FloatingPoint
     CNVRT r, x, address[,I]
     If F = 0, convert c(EA) to a fixed point number and store in r.
     If F = 1, convert c(EA) to a floating point number and store in FR0.
     The r register contains the value of F before the instruction is executed.
    */
    public static void convert_to_fixed_or_floating_point(){
        System.out.println("CNVRT");
    }

    /**
     OPCODE 41 - Load Index Register from Memory
     LDX x, address[,I]
     x = 1..3
     Xx <- c(EA)
    */
    public static void load_index_register_from_memory(){
        System.out.println("LDX");
    }

    /**
     OPCODE 42 - Store Index Register to Memory
     STX x, address[,I]
     X = 1..3
     Memory(EA) <- c(Xx)
    */
    public static void store_index_register_to_memory(){
        System.out.println("STX");
    }

    /**
     OPCODE 50 - Load Floating Register From Memory
     LDFR fr, x, address [,i]
     fr = 0..1
     fr <- c(EA), c(EA+1)
     fr <- c(c(EA), c(EA)+1), if I bit set
    */
    public static void load_floating_point_from_memory(){
    System.out.println("LDFR");
}

    /**
     OPCODE 51 - Store Floating Register To Memory
     STFR fr, x, address [,i]
     fr = 0..1
     EA, EA+1 <- c(fr)
     c(EA), c(EA)+1 <- c(fr), if I-bit set
    */
    public static void store_floating_point_to_memory(){
        System.out.println("STFR");
    }

    /**
     * OPCODE 61 - Input Character To Register from Device
     * IN r, devid
     * r = 0..3
     */
    public static void input_character_to_register_from_device(){
        System.out.println("IN");
    }

    /**
     OPCODE 62 - Output Character to Device from Register
     OUT r, devid
     r = 0..3
     */
    public static void output_character_to_device_from_register(){
        System.out.println("OUT");
    }

    /**
     OPCODE 63 - Check Device Status to Register
     CHK r, devid
     r = 0..3
     c(r) <- device status
    */
    public static void check_device_status_to_register(){
        System.out.println("CHECK");
    }


    public static void parse_and_execute(short word) {
        short opcode = Simulator.extract_opcode(word);

        switch(opcode) {
            case 0:
                halt();
                break;
            case 1:
                load_register_from_memory();
                break;
            case 2:
                store_register_to_memory();
                break;
            case 3:
                load_register_with_address();
                break;
            case 4:
                add_memory_to_register();
                break;
            case 5:
                subtract_memory_from_register();
                break;
            case 6:
                add_immediate_to_register();
                break;
            case 7:
                subtract_immediate_from_register();
                break;
            case 10:
                jump_if_zero();
                break;
            case 11:
                jump_if_not_zero();
                break;
            case 12:
                jump_if_condition_code();
                break;
            case 13:
                unconditional_jump_to_address();
                break;
            case 14:
                jump_and_save_return_address();
                break;
            case 15:
                return_from_subroutine();
                break;
            case 16:
                subtract_one_and_branch();
                break;
            case 17:
                jump_greater_than_or_equal_to();
                break;
            case 20:
                multiply_register_by_register();
                break;
            case 21:
                divide_register_by_register();
                break;
            case 22:
                test_the_equality_of_register_and_register();
                break;
            case 23:
                logical_and_of_register_and_register();
                break;
            case 24:
                logical_or_of_register_and_register();
                break;
            case 25:
                logical_not_of_register_and_register();
                break;
            case 31:
                shift_register_by_count();
                break;
            case 32:
                rotate_register_by_count();
                break;
            case 33:
                floating_add_memory_to_register();
                break;
            case 34:
                floating_subtract_memory_from_register();
                break;
            case 35:
                vector_add();
                break;
            // case 36:
            //   vector_substract();
            //   break;
            case 36:
                trap();
                break;
            case 37:
                convert_to_fixed_or_floating_point();
                break;
            case 41:
                load_index_register_from_memory();
                break;
            case 42:
                store_index_register_to_memory();
                break;
            case 50:
                load_floating_point_from_memory();
                break;
            case 51:
                store_floating_point_to_memory();
                break;
            case 61:
                input_character_to_register_from_device();
                break;
            case 62:
                output_character_to_device_from_register();
                break;
            case 63:
                check_device_status_to_register();
                break;
        }

    }

    // Given a 16-bit short, generates a binary string
    public static String word_to_string(short word){
        String binary_string = Integer.toBinaryString(Short.toUnsignedInt(word));
        return String.format("%1$16s", binary_string).replace(' ', '0');
    }
}
