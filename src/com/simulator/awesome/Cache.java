package com.simulator.awesome;

import java.util.ArrayList;

public class Cache {
    private ArrayList<CacheLine> contents;
    static final short capacity = 16;

    Cache(){
        this.contents = new ArrayList<>();
    }

    public short store(short words[]) {
        if (words[0] % 4 != 0) {
            throw new Error("CacheLine Alignment Error!");
        }
        if (words.length != 4) {
            throw new Error("A cache line must be four words!");
        }

    }

    /**
     * Fetches an address from the cache if present or null if it is not
     * @param address an address we are trying to fetch from the cache
     * @return the value from the cache if present and valid or null if not present
     */
    public Short fetch(short address) {
        short tag = Utils.short_unsigned_right_shift(address, 2);
        short offset = (short)(address % 4);
        CacheLine[] matches = this.contents.stream().filter(cacheLine -> cacheLine.getTag() == tag && cacheLine.isValid()).toArray(CacheLine[]::new);
        if (matches.length >= 1){
            return matches[0].getWord(offset);
        }
        return null;
    }

    public void updateIfPresent(short word){
        short tag = (short)((word / 4) * 4);
        short word = (short)(word % 4);
    }

}
