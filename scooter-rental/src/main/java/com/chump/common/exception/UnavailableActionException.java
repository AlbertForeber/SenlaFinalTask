package com.chump.common.exception;

public class UnavailableActionException extends RuntimeException {

    public UnavailableActionException(String message) {
        super(message);
    }

    public UnavailableActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
