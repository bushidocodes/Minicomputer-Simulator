package com.simulator.awesome;

public class IllegalMemoryAddressBeyondLimitException extends Exception {

    public IllegalMemoryAddressBeyondLimitException() {
        super();
    }

    public IllegalMemoryAddressBeyondLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IllegalMemoryAddressBeyondLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalMemoryAddressBeyondLimitException(String message) {
        super(message);
    }

    public IllegalMemoryAddressBeyondLimitException(Throwable cause) {
        super(cause);
    }
}