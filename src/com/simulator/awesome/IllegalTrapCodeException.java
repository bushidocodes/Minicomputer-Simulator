package com.simulator.awesome;

public class IllegalTrapCodeException extends Exception {

    public IllegalTrapCodeException() {
        super();
    }

    public IllegalTrapCodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IllegalTrapCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalTrapCodeException(String message) {
        super(message);
    }

    public IllegalTrapCodeException(Throwable cause) {
        super(cause);
    }
}