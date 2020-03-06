package com.simulator.awesome;

import static com.simulator.awesome.Utils.wordToString;

public class Memory {
    // The number of 16-bit words we have in memory
    private int wordCount;

    // The linear memory of our simulated system.
    // Shorts in Java are 16-bit, so this is word addressable
    // The index represents the nth word, zero indexed
    private Short memory[];

    // Memory Address Register. Holds the address of the word to be fetched from memory
    public MemoryAddressRegister mar;

    // Memory Buffer Register. Holds the word just fetched or the word to be stored into memory.
    // Also known as the Memory Data Register (MDR) in certain architectures
    private short mbr;

    // Cache
    public Cache cache;

    private Simulator context;

    Memory(Simulator context, int wordCount){
        this.context = context;

        // Allocate and zero out Linear Memory
        this.wordCount = Config.WORD_COUNT;
        this.memory = new Short[this.wordCount];

        this.mar = new MemoryAddressRegister();
        this.mbr = 0;

        for (int i = 0; i < this.wordCount; i++) {
            this.memory[i] = 0;
        }

        this.cache = new Cache(context);
    }

    public void reset(){
        this.mar = new MemoryAddressRegister();
        this.mbr = 0;
        for (int i = 0; i < this.wordCount; i++) {
            this.memory[i] = 0;
        }
        this.cache = new Cache(context);
    }

    public short getWordCount() {
        return (short) (this.wordCount);
    }

    public short getMemoryBufferRegister() {
        return this.mbr;
    }

    public void setMemoryBufferRegister(short mbr) {
        this.mbr = mbr;
    }

    // Given an address, get the block containing that word
    private short[] getBlock(short address){
        // Get the base block-aligned address
        short base = (short)(address & 0b1111111111111100);
        short[] result = {
                this.memory[base],
                this.memory[base + 1],
                this.memory[base + 2],
                this.memory[base + 3]
        };
        return result;
    }

    private short getWord(int address) {
        if (address < 6) {
            // Illegally accessing protected memory
            // In the future, we'll set MFR to xxx1, but for now, we can just halt
            this.context.io.engineerConsolePrintLn("Illegally accessing protected address " + address + "! Halting");

            this.context.msr.setIsRunning(false);
            if (!this.context.msr.isInteractive()) {
                this.context.io.engineerConsolePrintLn("Not Interactive");
                System.exit(1);
            }
        } else if (address > this.wordCount) {
            // Illegally accessing protecting memory above limit
            // In the future, we'll set MFT to 1xxx, but for now, we can just halt
            this.context.io.engineerConsolePrintLn("Illegally accessing address above highest memory address. Halting!");
            this.context.msr.setIsRunning(false);
            if (!this.context.msr.isInteractive()) {
                System.exit(1);
            }
        }

        Short cacheResult = this.cache.fetch((short)address);
        if (cacheResult != null) {
            this.context.io.engineerConsolePrintLn("Cache Hit! " + address + " was in cache!");
            return cacheResult;
        } else {
            short tag = Utils.short_unsigned_right_shift((short)address, 2);
            this.context.io.engineerConsolePrintLn("Cache Miss! Adding " + address + " as tag " + tag);
            this.cache.store(tag, this.getBlock((short)address));
            return this.memory[address];
        }
    }

    private void setWord(int address, short value){
        if (address < 6) {
            // Illegally accessing protected memory
            // In the future, we'll set MFR to xxx1, but for now, we can just halt
            this.context.io.engineerConsolePrintLn("setWord - Illegally accessing protecting address! Halting");
            this.context.msr.setIsRunning(false);
            if (!this.context.msr.isInteractive()) {
                System.exit(1);
            }
        } else if (address > this.wordCount) {
            // Illegally accessing protecting memory above limit
            // In the future, we'll set MFT to 1xxx, but for now, we can just halt
            this.context.io.engineerConsolePrintLn("Illegally accessing address above highest memory address. Halting!");
            this.context.msr.setIsRunning(false);
            if (!this.context.msr.isInteractive()) {
                System.exit(1);
            }
        } else {
            try {
                this.cache.updateIfPresent((short)address, value);
                this.memory[address] = value;
            } catch (Exception err) {
                System.err.println("Accessing " + address + " causes " + err);
            }
        }
    }

    public short fetch(short address) {
        this.mar.set(address);
        this.mbr = this.getWord(this.mar.get());
        return this.mbr;
    }

    public void store (short address, short value) {
        // MAR <- address
        this.mar.set(address);
        // MBR <- value
        this.mbr = value;
        // c(MAR) <- MBR
        this.context.memory.setWord(this.mar.get(), this.mbr);
    }

    public void dump(){
        this.context.io.engineerConsolePrintLn("===============================");
        this.context.io.engineerConsolePrintLn("Memory Subsystem Dump (Excluding zeroed out words");
        this.context.io.engineerConsolePrintLn("===============================");
        this.context.io.engineerConsolePrintLn("Memory Address Register: " + this.mar.toString());
        this.context.io.engineerConsolePrintLn("Memory Buffer Register: " + wordToString(this.getMemoryBufferRegister()));
        this.context.io.engineerConsolePrintLn("===============================");
        this.context.io.engineerConsolePrintLn("Linear Memory (Excluding zeroed out words");
        this.context.io.engineerConsolePrintLn("===============================");
        for (int i=0; i<this.wordCount; i++) {
            try{
                if (memory[i] != 0) this.context.io.engineerConsolePrintLn(String.format("Address: %4d: %s",  i, wordToString(memory[i])));
            } catch(NullPointerException e) {
                //null value in memory-- carry on
            }
        }
        this.cache.dump();
        this.context.io.engineerConsolePrintLn("===============================");
    }

}
