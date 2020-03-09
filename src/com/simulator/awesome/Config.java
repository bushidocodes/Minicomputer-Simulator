package com.simulator.awesome;

public class Config {
    public static final short WORD_COUNT = 2048;
    public static final short INPUT_IO_BUFFER_SIZE = 1024;
    public static final short OUTPUT_IO_BUFFER_SIZE = 1024;
    public static short MIN_VALUE = 0;
    public static final int MAX_VALUE = 65535; //can't use a short here because Java doesn't allow unsigned shorts
}
