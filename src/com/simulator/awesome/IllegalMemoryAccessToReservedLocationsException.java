package com.simulator.awesome;

public class IllegalMemoryAccessToReservedLocationsException extends Exception {

    public IllegalMemoryAccessToReservedLocationsException() {
        super();
    }

    public IllegalMemoryAccessToReservedLocationsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IllegalMemoryAccessToReservedLocationsException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalMemoryAccessToReservedLocationsException(String message) {
        super(message);
    }

    public IllegalMemoryAccessToReservedLocationsException(Throwable cause) {
        super(cause);
    }
}