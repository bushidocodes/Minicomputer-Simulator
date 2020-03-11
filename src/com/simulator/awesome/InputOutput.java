package com.simulator.awesome;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class InputOutput {
    final Simulator context;

    // IO buffers handle the connections between IO devices and the computer
    private final LinkedBlockingQueue[] outputBuffer;
    private final LinkedBlockingQueue[] inputBuffer;
    private final LinkedBlockingQueue engineerConsoleOutputBuffer;

    public InputOutput(Simulator context){
        this.context = context;
        this.outputBuffer = new LinkedBlockingQueue[Config.OUTPUT_IO_BUFFER_SIZE];
        this.inputBuffer = new LinkedBlockingQueue[Config.INPUT_IO_BUFFER_SIZE];
        this.engineerConsoleOutputBuffer = new LinkedBlockingQueue();
    }

    public void reset(){
        // Clear IO buffers and states
        this.context.msr.setReadyForInput(false);
        emptyInputBuffer();
        emptyOutputBuffer();
        emptyEngineersConsoleBuffer();
    }


    public void addWordToOutputBuffer(short deviceId, short word) {
        this.outputBuffer[deviceId].add(word);
    }

    public short getFirstWordFromOutputBuffer(short deviceId){
        return (short) outputBuffer[deviceId].remove();
    }

    public boolean isOutputBufferNull(short deviceId){
        return outputBuffer[deviceId].peek() == null;
    }

    public void emptyOutputBuffer(){
        for (int i=0; i<outputBuffer.length; i++) {
            outputBuffer[i].clear();
        }
    }

    public void addWordToInputBuffer(short deviceId, short inputBuffer) {
        this.inputBuffer[deviceId].add(inputBuffer);
    }

    public short getFirstWordFromInputBuffer(short deviceId){
        return (short) inputBuffer[deviceId].remove();
    }

    public boolean isInputBufferNull(short deviceId){
        return inputBuffer[deviceId].peek() == null;
    }

    public void emptyInputBuffer(){
        for (int i=0; i<inputBuffer.length; i++) {
            inputBuffer[i].clear();
        }
    }

    public boolean isEngineersConsoleBufferNull(){
        return engineerConsoleOutputBuffer.peek() == null;
    }

    public void emptyEngineersConsoleBuffer(){
        engineerConsoleOutputBuffer.clear();
    }

    // Prints a line of text to the engineer's console
    public void engineerConsolePrintLn(String outputString){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date timestamp = new Date();
        engineerConsoleOutputBuffer.add("["+formatter.format(timestamp)+"]: "+outputString+"\n");
    }

    public String getFirstLineFromEngineersOutputBuffer(){
        return String.valueOf(engineerConsoleOutputBuffer.remove());
    }

    public void initializeIOBuffers(){
        // Initialize LinkedBlockingQueues for IO buffers
        for(int i=0; i<outputBuffer.length; i++){
            outputBuffer[i] = new LinkedBlockingQueue();
            inputBuffer[i] = new LinkedBlockingQueue();
        }
    }

    public int getSizeOfInputBuffer(short deviceId){
        return inputBuffer[deviceId].size();
    }
    public int getSizeOfOutputBuffer(short deviceId){
        return outputBuffer[deviceId].size();
    }
}
