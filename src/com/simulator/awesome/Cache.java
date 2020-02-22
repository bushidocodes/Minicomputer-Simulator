package com.simulator.awesome;

import java.util.ArrayList;

public class Cache {
    private ArrayList<CacheLine> contents;
    static final short capacity = 16;

    Cache(){
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
        System.out.println("=======================================");
        System.out.println("Cache");
        System.out.println("=======================================");
        this.contents.stream().forEach(cacheLine -> {
            System.out.println("Tag: " + cacheLine.getTag());
            System.out.println("    Word 0: " + cacheLine.getWord((short)0));
            System.out.println("    Word 1: " + cacheLine.getWord((short)1));
            System.out.println("    Word 2: " + cacheLine.getWord((short)2));
            System.out.println("    Word 3: " + cacheLine.getWord((short)3));
        });
    }

}
