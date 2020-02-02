package com.simulator.awesome;

import java.util.Arrays;

public class Simulator {

    // The number of 16-bit words we have in memory
    private int wordCount;

    // The linear memory of our simulated system.
    // Shorts in Java are 16-bit, so this is word addressable
    // The index represents the nth word, zero indexed
    private Short memory[];

    // Program Counter: Address of the next instruction to be executed
    // Uses the least significant 12 bits
    private short pc;
    static final short PC_MASK = (short)0b0000111111111111;

    // Condition code.
    // Uses the least significant 4 bits
    // set when arithmetic/logical operations are executed
    // it has four 1-bit elements: overflow, underflow, division by zero, equal-or-not.
    // ID   |   Fault
    // 0001 |   Overflow
    // 0010 |   Underflow
    // 0100 |   Division by Zero
    // 1000 |   Equal Or Not
    private byte cc;

    // Instruction register. Holds the instruction to be executed
    private short ir;

    // Memory Address Register. Holds the address of the word to be fetched from memory
    private short mar;
    static final short MAR_MASK = (short)0b0000111111111111;

    // Memory Buffer Register.
    // Holds the word just fetched or the word to be stored into memory.
    // If this is a word we're writing to memory, it can either be something to be written
    // or something that we've already written, ya dig?
    private short mbr;

    // Machine Fault Register.
    // Contains the ID code of a machine fault after it occurs
    // DO NOT IMPLEMENT UNTIL PHASE 3
    // ID   |   Fault
    // 0001 |   Illegal Memory Address to Reserved Locations
    // 0010 |   Illegal TRAP code
    // 0100 |   Illegal Operation Code
    // 1000 |   Illegal Memory Address beyond 2048 (or higher is memory is expanded)
    private byte mfr;

    // The four general purpose registers
    private short r0, r1, r2, r3;

    // The three index registers
    private short x1, x2, x3;

    /** The execution step, 1-6
     * 1. Instruction Fetch
     * 2. Instruction Decode
     * 3. Operand Fetch
     * 4. Execute
     * 5. Result Store
     * 6. Next Instruction
     **/
    private int executionStep = 1;

    private boolean isRunning = false;

    // TODO: What are the mystery registers we're missing?

    Simulator(int wordCount) {
        // Allocate Linear Memory
        this.wordCount = 2048;
        this.memory = new Short[wordCount];

        for (int i = 0; i < wordCount; i++) {
            this.memory[i] = 0;
        }

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

    public short getWord(int address) {
        if (address < 6) {
            // Illegally accessing protected memory
            // In the future, we'll set MFR to xxx1, but for now, we can just halt
            System.out.println("Illegally accessing protecting address! Halting");
            halt();
        } else if (address > this.wordCount) {
            // Illegally accessing protecting memory above limit
            // In the future, we'll set MFT to 1xxx, but for now, we can just halt
            System.out.println("Illegally accessing address above highest memory address. Halting!");
            halt();
        }

        return this.memory[address];
    }

    public void setWord(int address, short value){
        if (address < 6) {
            // Illegally accessing protected memory
            // In the future, we'll set MFR to xxx1, but for now, we can just halt
            System.out.println("Illegally accessing protecting address! Halting");
            halt();
        } else if (address > this.wordCount) {
            // Illegally accessing protecting memory above limit
            // In the future, we'll set MFT to 1xxx, but for now, we can just halt
            System.out.println("Illegally accessing address above highest memory address. Halting!");
            halt();
        } else {
            try {
                this.memory[address] = value;

            } catch (Exception err) {
                System.err.println("Accessing " + address + " causes " + err);
            }
        }
    }

    public short getProgramCounter() {
        return (short) (this.pc & Simulator.PC_MASK);
    }

    public void setProgramCounter(short unmaskedPC) {
        this.pc = (short) (unmaskedPC & Simulator.PC_MASK);
    }

    /**
     * A Helper getter for bit arrays
     * @param bitArray - The integral value that we are treating as an array of bits
     * @param offset - The offset from the least significant bit. 0 is b0001
     * @return - True if bit is set
     */
    private static boolean getNthLeastSignificantBit(int bitArray, int offset) {
        int getterMask = (0b00000001<<offset);
        return (bitArray & getterMask) == getterMask;
    }

    /**
     * A Helper setting for bit arrays
     * @param bitArray - The integral that we are treating as an array of bits
     * @param offset - The offset from the least significant bit. 0 is b0001
     * @param isSet - The resulting bit array after manipulating the bit
     */
    private static int setNthLeastSignificantBit(int bitArray, int offset, boolean isSet) {
        int setterMask = (0b00000001<<offset);
        return isSet ? (bitArray | setterMask) : (bitArray & ~setterMask);
    }

    public boolean isOverflow() {
        return getNthLeastSignificantBit(this.cc, 0);
    }

    public void setOverflow(boolean isOverflow) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 0, isOverflow);
    }

    public boolean isUnderflow() {
        return getNthLeastSignificantBit(this.cc, 1);
    }

    public void setUnderflow(boolean isUnderflow) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 1, isUnderflow);
    }

    public boolean isDivideByZero() {
        return getNthLeastSignificantBit(this.cc, 2);
    }

    public void setDivideByZero(boolean isDivideByZero) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 2, isDivideByZero);
    }

    public boolean isEqualOrNot() {
        return getNthLeastSignificantBit(this.cc, 3);
    }

    public void setEqualOrNot(boolean isEqualOrNot) {
        this.cc = (byte)setNthLeastSignificantBit(this.cc, 3, isEqualOrNot);
    }

    public short getMemoryAddressRegister() {
        return (short) (this.mar & Simulator.MAR_MASK);
    }

    public void setMemoryAddressRegister(short unmaskedMAR) {
        this.mar = (short) (unmaskedMAR & Simulator.PC_MASK);
    }

    public boolean isIllegalMemoryAccessToReservedLocations() {
        return getNthLeastSignificantBit(this.mfr, 0);
    }

    public void setIllegalMemoryAccessToReservedLocations(boolean isIllegalMemoryAccessToReservedLocations) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 0, isIllegalMemoryAccessToReservedLocations);
    }

    public boolean isIllegalTrapCode() {
        return getNthLeastSignificantBit(this.mfr, 1);
    }

    public void setIllegalTrapCode(boolean isIllegalTrapCode) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 1, isIllegalTrapCode);
    }

    public boolean isIllegalOpcode() {
        return getNthLeastSignificantBit(this.mfr, 2);
    }

    public void setIsIllegalOpcode(boolean isIllegalOpcode) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 2, isIllegalOpcode);
    }

    public boolean isIllegalMemoryAddressBeyondLimit() {
        return getNthLeastSignificantBit(this.mfr, 3);
    }

    public void setIsIllegalMemoryAddressBeyondLimit(boolean isIllegalMemoryAddressBeyondLimit) {
        this.mfr = (byte)setNthLeastSignificantBit(this.mfr, 3, isIllegalMemoryAddressBeyondLimit);
    }

    public boolean isRunning(){
        return isRunning;
    }

    public void setIsRunning(boolean running){
        if(running){
            isRunning = true;
        } else {
            isRunning = false;
        }
    }

    /**
     *
     * @param word - the machine word you want to extract the OPCODE from
     * @return - The value of the opcode from the word
     */
    private static short extractOpCode(short word) {
        // OPCODE is 0-5bits, so right shift by 10
        // the >>> operator is a bitshift that includes the "sign bit"
        short opcode = (short)(word>>>10);
        return opcode;
    }

    /**
     OPCODE 00 - Halt the Machine
     Octal: 000
     HLT
    */
    private void halt() {
        System.out.println("Halting...");
        System.exit(0);
    }

    /**
     OPCODE 01 - Load Register From Memory
     Octal: 001
     LDR r, x, address[,I]
     r = 0..3
     r <- c(EA)
     note that EA is computed as given above

     In one cycle, move the data from the MBR to an Internal Result Register (IRR)
     */
    private void loadRegisterFromMemory(){
        System.out.println("LDR");
    }

    /**
     OPCODE 02 - Store Register To Memory
     Octal: 002
     STR r, x, address[,I]
     r = 0..3
     Memory(EA) <- c(r)
    */
    private void storeRegisterToMemory(){
        System.out.println("STR");
    }

    /**
     OPCODE 03 - Load Register with Address
     Octal: 003
     LDA r, x, address[,I]
     r = 0..3
     r <- EA
    */
    private void loadRegisterWithAddress(){
        System.out.println("LDA");
    }

    /**
     OPCODE 04 - Add Memory To Register
     Octal: 004
     AMR r, x, address[,I]
     r = 0..3
     r<- c(r) + c(EA)
    */
    private void addMemoryToRegister(){
        System.out.println("AMR");
    }

    /**
     OPCODE 05 - Subtract Memory From Register
     Octal: 005
     SMR r, x, address[,I]
     r = 0..3
     r<- c(r) – c(EA)
    */
    private void subtractMemoryFromRegister(){
        System.out.println("SMR");
    }

    /**
     * OPCODE 06 - Add Immediate to Register
     * Octal: 006
     * AIR r, immed
     * r = 0..3
     * r <- c(r) + Immed
     * Note:
     *  1. if Immed = 0, does nothing
     *  2. if c(r) = 0, loads r with Immed
     *  IX and I are ignored in this instruction
     */
    private void addImmediateToRegister(){
        System.out.println("AIR");
    }


    /**
     OPCODE 07 - Subtract Immediate from Register
     Octal: 007
     SIR r, immed
     r = 0..3
     r <- c(r) - Immed
     Note:
         1. if Immed = 0, does nothing
         2. if c(r) = 0, loads r1 with –(Immed)
         IX and I are ignored in this instruction
    */
    private void subtractImmediateFromRegister(){
        System.out.println("SIR");
    }

    /**
     OPCODE 10 - Jump If Zero
     Octal: 012
     JZ r, x, address[,I]
     If c(r) = 0, then PC <- EA
     Else PC <- PC+1
    */
    private void jumpIfZero(){
        System.out.println("JZ");
    }

    /**
     OPCODE 11 - Jump If Not Equal
     Octal: 013
     JNE r, x, address[,I]
     If c(r) != 0, then PC <-- EA
     Else PC <- PC + 1
    */
    private void jumpIfNotZero(){
        System.out.println("JZ");
    }

    /**
     * OPCODE 12 - Jump If Condition Code
     * Octal: 014
     * JCC cc, x, address[,I]
     * cc replaces r for this instruction
     * cc takes values 0, 1, 2, 3 as above and specifies the bit in the Condition Code Register to check;
     * If cc bit  = 1, PC <- EA
     * Else PC <- PC + 1
     **/
    private void jumpIfConditionCode(){
        System.out.println("JCC");
    }

    /**
     * OPCODE 13 - Unconditional Jump To Address
     * Octal: 015
     * JMA x, address[,I]
     * PC <- EA,
     * Note: r is ignored in this instruction
     */
    private void unconditionalJumpToAddress(){
        System.out.println("JMA");
    }

    /**
     OPCODE 14 - Jump and Save Return Address
     Octal: 016
     JSR x, address[,I]
     R3 <- PC+1;
     PC <- EA
     R0 should contain pointer to arguments
     Argument list should end with –1 (all 1s) value
    */
    private void jumpAndSaveReturnAddress(){
        System.out.println("JSR");
    }

    /**
     OPCODE 15 - Return From Subroutine
     Octal: 017
     w/ return code as Immed portion (optional) stored in the instruction’s address field.
     RFS Immed
     R0 <- Immed; PC <- c(R3)
     IX, I fields are ignored.
    */
    private void returnFromSubroutine(){
        System.out.println("RFS");
    }

    /**
     OPCODE 16 - Subtract One and Branch.
     Octal: 020
     SOB r, x, address[,I]
     r = 0..3
     r <- c(r) – 1
     If c(r) > 0,  PC <- EA;
     Else PC <- PC + 1
    */
    private void subtractOneAndBranch(){
        System.out.println("SOB");
    }

    /**
     OPCODE 17 - Jump Greater Than or Equal To
     Octal: 021
     JGE r,x, address[,I]
     If c(r) >= 0, then PC <- EA
     Else PC <- PC + 1
    */
    private void jumpGreaterThanOrEqualTo(){
        System.out.println("JGE");
    }

    /**
     OPCODE 20 - Multiply Register by Register
     Octal: 024
     MLT rx,ry
     rx, rx+1 <- c(rx) * c(ry)
     rx must be 0 or 2
     ry must be 0 or 2
     rx contains the high order bits, rx+1 contains the low order bits of the result
     Set OVERFLOW flag, if overflow
    */
    private void multiplyRegisterByRegister(){
        System.out.println("MLT");
    }

    /**
     OPCODE 21 - Divide Register by Register
     Octal: 025
     DVD rx,ry
     rx, rx+1 <- c(rx)/ c(ry)
     rx must be 0 or 2
     rx contains the quotient; rx+1 contains the remainder
     ry must be 0 or 2
     If c(ry) = 0, set cc(3) to 1 (set DIVZERO flag)
    */
    private void divideRegisterByRegister(){
        System.out.println("DVD");
    }

    /**
     OPCODE 22 - Test the Equality of Register and Register
     Octal: 026
     TRR rx, ry
     If c(rx) = c(ry), set cc(4) <- 1; else, cc(4) <- 0
    */
    private void testTheEqualityOfRegisterAndRegister(){
        System.out.println("TRR");
    }

    /**
     OPCODE 23 - Logical And of Register and Register
     Octal: 027
     AND rx, ry
     c(rx) <- c(rx) AND c(ry)
    */
    private void logicalAndOfRegisterAndRegister(){
        System.out.println("AND");
    }

    /**
     OPCODE 24 - Logical Or of Register and Register
     Octal: 030
     ORR rx, ry
     c(rx) <- c(rx) OR c(ry)
    */
    private void logicalOrOfRegisterAndRegister(){
        System.out.println("ORR");
    }

    /**
     OPCODE 25 - Logical Not of Register To Register
     Octal: 031
     NOT rx
     C(rx) <- NOT c(rx)
    */
    private void logicalNotOfRegisterAndRegister(){
        System.out.println("NOT");
    }

    /**
     * OPCODE 30 - Trap
     * Octal: 036
     * Traps to memory address 0, which contains the address of a table in memory.
     * Stores the PC+1 in memory location 2.
     * The table can have a maximum of 16 entries representing 16 routines for user-specified instructions stored elsewhere in memory.
     * Trap code contains an index into the table, e.g. it takes values 0 – 15.
     * When a TRAP instruction is executed, it goes to the routine whose address is in memory location 0,
     * executes those instructions, and returns to the instruction stored in memory location 2.
     * The PC+1 of the TRAP instruction is stored in memory location 2.
     */
    private void trap(){
        System.out.println("TRAP");
    }

    /**
     OPCODE 31 - Shift Register by Count
     Octal: 037
     SRC r, count, L/R, A/L
     c(r) is shifted left (L/R =1) or right (L/R = 0) either logically (A/L = 1) or arithmetically (A/L = 0)
     XX, XXX are ignored
     Count = 0…15
     If Count = 0, no shift occurs
    */
    private void shiftRegisterByCount(){
        System.out.println("SRC");
    }

    /**
     OPCODE 32 - Rotate Register by Count
     Octal: 040
     RRC r, count, L/R, A/L
     c(r) is rotated left (L/R = 1) or right (L/R =0) either logically (A/L =1)
     XX, XXX is ignored
     Count = 0…15
     If Count = 0, no rotate occurs
    */
    private void rotateRegisterByCount(){
        System.out.println("RRC");
    }

    /**
     OPCODE 33 - Floating Add Memory To Register
     Octal: 041
     FADD fr, x, address[,I]
     c(fr) <- c(fr) + c(EA)
     c(fr) <- c(fr) + c(c(EA)), if I bit set
     fr must be 0 or 1.
     OVERFLOW may be set
    */
    private void floatingAddMemoryToRegister(){
        System.out.println("FADD");
    }

    /**
     OPCODE 34 - Floating Subtract Memory From Register
     Octal: 042
     FSUB fr, x, address[,I]
     c(fr) <- c(fr) - c(EA)
     c(fr) <- c(fr) - c(c(EA)), if I bit set
     fr must be 0 or 1
     UNDERFLOW may be set
    */
    private void floatingSubtractMemoryFromRegister(){
        System.out.println("FSUB");
    }

    /**
     OPCODE 35 - Vector Add
     Octal: 043
     VADD fr, x, address[,I]
     fr contains the length of the vectors
     c(EA) or c(c(EA)), if I bit set, is address of first vector
     c(EA+1) or c(c(EA+1)), if I bit set, is address of the second vector
     Let V1 be vector at address; Let V2 be vector at address+1
     Then, V1[i] = V1[i]+ V2[i], i = 1, c(fr).
    */
    private void vectorAdd(){
        System.out.println("VADD");
    }

    /**
     OPCODE 36 - Vector Subtract
     Octal: 044
     VSUB fr, x, address[,I]
     fr contains the length of the vectors
     c(EA) or c(c(EA)), if I bit set is address of first vector
     c(EA+1) or c(c(EA+1)), if I bit set is address of the second vector
     Let V1 be vector at address; Let V2 be vector at address+1
     Then, V1[i] = V1[i] - V2[i], i = 1, c(fr).
    */
    private void vectorSubstract(){
        System.out.println("VSUB");
    }

    /**
     OPCODE 37 - Convert to Fixed/FloatingPoint
     Octal: 045
     CNVRT r, x, address[,I]
     If F = 0, convert c(EA) to a fixed point number and store in r.
     If F = 1, convert c(EA) to a floating point number and store in FR0.
     The r register contains the value of F before the instruction is executed.
    */
    private void convertToFixedOrFloatingPoint(){
        System.out.println("CNVRT");
    }

    /**
     OPCODE 41 - Load Index Register from Memory
     Octal: 051
     LDX x, address[,I]
     x = 1..3
     Xx <- c(EA)
    */
    private void loadIndexRegisterFromMemory(){
        System.out.println("LDX");
    }

    /**
     OPCODE 42 - Store Index Register to Memory
     Octal: 052
     STX x, address[,I]
     X = 1..3
     Memory(EA) <- c(Xx)
    */
    private void storeIndexRegisterToMemory(){
        System.out.println("STX");
    }

    /**
     OPCODE 50 - Load Floating Register From Memory
     Octal: 062
     LDFR fr, x, address [,i]
     fr = 0..1
     fr <- c(EA), c(EA+1)
     fr <- c(c(EA), c(EA)+1), if I bit set
    */
    private void loadFloatingPointFromMemory(){
    System.out.println("LDFR");
}

    /**
     OPCODE 51 - Store Floating Register To Memory
     Octal: 063
     STFR fr, x, address [,i]
     fr = 0..1
     EA, EA+1 <- c(fr)
     c(EA), c(EA)+1 <- c(fr), if I-bit set
    */
    private void storeFloatingPointToMemory(){
        System.out.println("STFR");
    }

    /**
     * OPCODE 61 - Input Character To Register from Device
     * Octal: 075
     * IN r, devid
     * r = 0..3
     */
    private void inputCharacterToRegisterFromDevice(){
        System.out.println("IN");
    }

    /**
     OPCODE 62 - Output Character to Device from Register
     Octal: 076
     OUT r, devid
     r = 0..3
     */
    private void outputCharacterToDeviceFromRegister(){
        System.out.println("OUT");
    }

    /**
     OPCODE 63 - Check Device Status to Register
     Octal: 077
     CHK r, devid
     r = 0..3
     c(r) <- device status
    */
    private void checkDeviceStatusToRegister(){
        System.out.println("CHECK");
    }


    public void parseAndExecute(short word) {
        short opCode = Simulator.extractOpCode(word);

        switch(opCode) {
            case 0:
                halt();
                break;
            case 1:
                loadRegisterFromMemory();
                break;
            case 2:
                storeRegisterToMemory();
                break;
            case 3:
                loadRegisterWithAddress();
                break;
            case 4:
                addMemoryToRegister();
                break;
            case 5:
                subtractMemoryFromRegister();
                break;
            case 6:
                addImmediateToRegister();
                break;
            case 7:
                subtractImmediateFromRegister();
                break;
            case 10:
                jumpIfZero();
                break;
            case 11:
                jumpIfNotZero();
                break;
            case 12:
                jumpIfConditionCode();
                break;
            case 13:
                unconditionalJumpToAddress();
                break;
            case 14:
                jumpAndSaveReturnAddress();
                break;
            case 15:
                returnFromSubroutine();
                break;
            case 16:
                subtractOneAndBranch();
                break;
            case 17:
                jumpGreaterThanOrEqualTo();
                break;
            case 20:
                multiplyRegisterByRegister();
                break;
            case 21:
                divideRegisterByRegister();
                break;
            case 22:
                testTheEqualityOfRegisterAndRegister();
                break;
            case 23:
                logicalAndOfRegisterAndRegister();
                break;
            case 24:
                logicalOrOfRegisterAndRegister();
                break;
            case 25:
                logicalNotOfRegisterAndRegister();
                break;
            case 30:
                trap();
                break;
            case 31:
                shiftRegisterByCount();
                break;
            case 32:
                rotateRegisterByCount();
                break;
            case 33:
                floatingAddMemoryToRegister();
                break;
            case 34:
                floatingSubtractMemoryFromRegister();
                break;
            case 35:
                vectorAdd();
                break;
            case 36:
                vectorSubstract();
                break;
            case 37:
                convertToFixedOrFloatingPoint();
                break;
            case 41:
                loadIndexRegisterFromMemory();
                break;
            case 42:
                storeIndexRegisterToMemory();
                break;
            case 50:
                loadFloatingPointFromMemory();
                break;
            case 51:
                storeFloatingPointToMemory();
                break;
            case 61:
                inputCharacterToRegisterFromDevice();
                break;
            case 62:
                outputCharacterToDeviceFromRegister();
                break;
            case 63:
                checkDeviceStatusToRegister();
                break;
        }
    }

    // Given a 16-bit short, generates a binary string
    public static String wordToString(short word){
        String binaryString = Integer.toBinaryString(Short.toUnsignedInt(word));
        return String.format("%1$16s", binaryString).replace(' ', '0');
    }

    public void loadProgram(String[] assembledMachineCode, int memoryLocation){
        // Iterate through memory line-by-line, loading in the corresponding machine code from the program.
        int computerMemoryLoc = memoryLocation;
        for (int inputMemoryLoc = 0; inputMemoryLoc<assembledMachineCode.length; inputMemoryLoc++){
            this.setWord(computerMemoryLoc, (short)Integer.parseUnsignedInt(assembledMachineCode[inputMemoryLoc],2));
            computerMemoryLoc++;
        }
    }

    // This function allows you to not specify a memory location for loading a program (default to location 6)
    public void loadProgram(String[] assembledMachineCode){
        // No memory location was specified, load the program into memory starting at the first location 6
        loadProgram(assembledMachineCode, 6);
    }

    // Not sure if we'll keep this but it's useful for printing the memory contents to console.
    public String[] memoryToString(){
        // Initialize an array equal to the computer memory size
        String[] memoryStringArr = new String[this.wordCount];

        // For each word in memory, convert it to a string of binary numbers
        for (int i=0; i<this.wordCount; i++) {
            try{
                memoryStringArr[i] = Simulator.wordToString(memory[i]);
            } catch(NullPointerException e) {
                //null value in memory-- carry on
            }
        }
        return memoryStringArr;
    }

    /** The execution step, 1-6
     * 1. Instruction Fetch
     * 2. Instruction Decode
     * 3. Operand Fetch
     * 4. Execute
     * 5. Result Store
     * 6. Next Instruction
     **/

    private void singleStep(){
        switch (executionStep){
            // Instruction Fetch
            case 1:
                executionInstructionFetch();
                break;
            // Instruction Decode
            case 2:
                executionInstructionDecode();
                break;
            // Operand Fetch
            case 3:
                executionOperandFetch();
                break;
            // Execute
            case 4:
                executionExecute();
                break;
            // Result Store
            case 5:
                executionResultStore();
                break;
            // Next Instruction
            case 6:
                executionNextInstruction();
                break;
        }
        if (executionStep == 6){
            executionStep = 1;
        } else {
            executionStep++;
        }
    }

    // Execution Step 1
    // Obtain Instruction from Program Storage
    private void executionInstructionFetch() {
        // MAR <- PC
        // Transfer Program Counter to Memory Address Register
        this.mar = this.pc;

        // MBR <-MEM[MAR]
        // Fetch the word located at the MAR location from memory and transfer it to Memory Buffer Register.
        this.mbr = getWord(this.mar);
    }

    // Execution Step 2
    // Determine Operation Required
    private void executionInstructionDecode() {
        // IR <- MBR
        // Transfer Memory Buffer Register to Instruction Register
        this.ir = this.mbr;

        // Extract the opcode from the IR
        short opcode = Simulator.extractOpCode(this.ir);

        // TODO: determine the class of opcode: determines the functional unit that will be used to execute the instruction
        // TODO: set internal flags based on opcode
    }

    // Execution Step 3
    // Locate and Fetch Operand Data
    private void executionOperandFetch() {
        // IAR <- IR(address field)
        // Move the first operand address from the Instruction Register to the Internal Address Register
        // TODO: What is the internal address register?

        // IAR<- IAR + X(index field)
        // If the operand is indexed, add the contents of the specified index register to the IAR
        // TODO: What is the internal address register?

        // MAR <- IR
        // Move the contents of the IAR to the MAR
        // TODO: What is the internal address register?

        // Fetch the contents of the word in memory specified by the MAR into the MBR.
        // "Read"
        // TODO: Is this correct?
        this.mbr = getWord(this.mar);

    }

    // Execution Step 4
    // Execute the Operation
    private void executionExecute() {
        // Depending on the operation code, execute the operation.
        //parseAndExecute(opcode);
        // TODO: where do we get the opcode?
    }

    // Execution Step 5
    // Deposit Results
    private void executionResultStore() {
        /**
        In 1 cycle, move the contents of the IRR to:
        a. If target = register, use Register Select 1 to store IRR contents into the specified register
        b. If target = memory, such as a STR, move contents of IRR to MBR. On the next cycle, move contents of MBR to memory using address in MAR.
         **/
        // TODO: implement this
    }

    // Execution Step 6
    // Determine Next Instruction
    private void executionNextInstruction() {
        // TODO: This will need to be expanded when we implement branching
        // Increment the Program Counter
        if (this.pc<this.wordCount){
            this.pc++;
        } else {
            pauseExecutionLoop();
        }
    }

    public void powerOn(){
        this.pc = 6;
    }

    public void startExecutionLoop(){
        System.out.println("Starting execution loop.");
        this.setIsRunning(true);
        while(this.isRunning){
            singleStep();
        }
    }

    public void pauseExecutionLoop(){
        System.out.println("Pausing execution loop.");
        this.setIsRunning(false);
    }
}