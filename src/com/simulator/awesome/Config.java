package com.simulator.awesome;

public class Config {
    public static short WORD_COUNT = 2048;
    public static short INPUT_IO_BUFFER_SIZE = 1024;
    public static short OUTPUT_IO_BUFFER_SIZE = 1024;
    public static short MIN_VALUE = 0;
    public static int MAX_VALUE = 65535; //can't use a short here because Java doesn't allow unsigned shorts
}
