package com.chump.common.exception;

public class UnavaliableActionException extends RuntimeException {

    public UnavaliableActionException(String message) {
        super(message);
    }

    public UnavaliableActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
