package com.simulator.awesome;

public class DataSet {
    private short   baseAddress;
    private short   numberOfBodySections;
    private short[] dataSet;

    DataSet(short[] data){
        this.baseAddress = 0;
        this.numberOfBodySections = 0;
        if (data.length == 0) throw new Error("data is empty!");
        if (data.length <= 31){
            this.dataSet = new short[32];
            this.dataSet[0] = (short)(data.length);
            for (int i = 0; i < data.length; i++){
                dataSet[i+1] = data[i];
            }
        } else {
            this.numberOfBodySections = (short)(Math.ceil(data.length / 32f));
            if (numberOfBodySections > 31) throw new Error("A Deep Table Dataset only supports 31 body sections");
            int sizeInWords = 32 * numberOfBodySections + 32;
            this.dataSet = new short[sizeInWords];
            // Add size to first word of header
            this.dataSet[0] = (short)(data.length);
            // Add indirect for each section to subsequent words of header
            for (int bodySection=1, address=this.baseAddress + 32; bodySection <= this.numberOfBodySections; bodySection++, address+=32){
                this.dataSet[bodySection] = (short)address;
            };
            // Copy the words
            for (int i = 0; i < data.length; i++){
                this.dataSet[i+32] = data[i];
            }
        }
    }

    public void setBaseAddress(short baseAddress) {
        int displacement = baseAddress - this.baseAddress;
        this.baseAddress = baseAddress;
        for (int bodySection=1, address=this.baseAddress + 32; bodySection <= this.numberOfBodySections; bodySection++, address+=32){
            this.dataSet[bodySection] += displacement;
        };
    }

    public short[] export(){
        return this.dataSet;
    }
}
