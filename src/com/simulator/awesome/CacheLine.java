package com.simulator.awesome;

// Intention is to model a 80-bit sequence of bits
// Tag (10 bit) | Valid Bit | 5 reserved Bits | word | word | word | word
public class CacheLine {
    short header;
    short words[];
    static short validBit = 0b100000;

    CacheLine(short tag, short firstWord, short secondWord, short thirdWord, short fourthWord){
        this.header = (short)((tag << 6 ) & CacheLine.validBit);
        this.words = new short[4];
        this.words[0] = firstWord;
        this.words[1] = secondWord;
        this.words[2] = thirdWord;
        this.words[3] = fourthWord;
    }

    public void clear() {
        this.header = 0;
        this.words = new short[4];
        this.words[0] = 0;
        this.words[1] = 0;
        this.words[2] = 0;
        this.words[3] = 0;
    }

    public short getTag(){
        return Utils.short_unsigned_right_shift(this.header, 6);
    }

    public short getWord(short nth){
        if (nth < 0 || nth > 3){
            throw new Error("Invalid index. Cache Line Expected 0 - 3");
        }

        return this.words[nth];
    }

    public void invalidateWord(){
        this.header &= ~CacheLine.validBit;
    }

    public boolean isValid(){
        return Utils.getNthLeastSignificantBit(this.header, 5 );
    }
}
