package com.simulator.awesome;

import java.util.ArrayList;

public class Cache {
    private ArrayList<CacheLine> contents;
    static final short capacity = 16;
    private Simulator context;

    Cache(Simulator context){
        this.context = context;
        this.contents = new ArrayList<>();
    }

    public void store(short tag, short words[]) {
        if (words.length != 4) throw new Error("A cache line must be four words!");

        if (this.contents.size() == Cache.capacity) this.contents.remove(0);

        // Add new cache line
        this.contents.add(new CacheLine(tag, words));
    }

    /**
     * Fetches an address from the cache if present or null if it is not
     * @param address an address we are trying to fetch from the cache
     * @return the value from the cache if present and valid or null if not present
     */
    public Short fetch(short address) {
        short tag = Utils.short_unsigned_right_shift(address, 2);
        short offset = (short)(address % 4);
        CacheLine[] matches = this.contents.stream().filter(cacheLine -> {
            short cacheTag = cacheLine.getTag();
            boolean cacheIsValid = cacheLine.isValid();
            return cacheTag == tag && cacheIsValid;
        }).toArray(CacheLine[]::new);
        return matches.length >= 1 ?  matches[0].getWord(offset) : null;
    }

    public void updateIfPresent(short address, short word){
        short tag = Utils.short_unsigned_right_shift(address, 2);
        short offset = (short)(address % 4);
        CacheLine[] matches = this.contents.stream().filter(cacheLine -> cacheLine.getTag() == tag && cacheLine.isValid()).toArray(CacheLine[]::new);
        if (matches.length >= 1) matches[0].setWord(offset, word);
    }

    public void dump(){
        this.context.engineerConsolePrintLn("===============================");
        this.context.engineerConsolePrintLn("Cache");
        this.context.engineerConsolePrintLn("===============================");
        if (this.contents.size() == 0) this.context.engineerConsolePrintLn("Cache Empty!");
        this.contents.stream().forEach(cacheLine -> {
            this.context.engineerConsolePrintLn("Tag: " + cacheLine.getTag());
            this.context.engineerConsolePrintLn("    Word 0: " + Simulator.wordToString(cacheLine.getWord((short)0)));
            this.context.engineerConsolePrintLn("    Word 1: " + Simulator.wordToString(cacheLine.getWord((short)1)));
            this.context.engineerConsolePrintLn("    Word 2: " + Simulator.wordToString(cacheLine.getWord((short)2)));
            this.context.engineerConsolePrintLn("    Word 3: " + Simulator.wordToString(cacheLine.getWord((short)3)));
        });
    }

}
