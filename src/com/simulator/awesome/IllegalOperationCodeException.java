package com.simulator.awesome;

public class IllegalOperationCodeException extends Exception {

    public IllegalOperationCodeException() {
        super();
    }

    public IllegalOperationCodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IllegalOperationCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalOperationCodeException(String message) {
        super(message);
    }

    public IllegalOperationCodeException(Throwable cause) {
        super(cause);
    }
}