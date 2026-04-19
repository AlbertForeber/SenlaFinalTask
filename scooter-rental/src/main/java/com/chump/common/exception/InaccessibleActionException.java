package com.chump.common.exception;

public class InaccessibleActionException extends RuntimeException {

    public InaccessibleActionException(String message) {
        super(message);
    }

    public InaccessibleActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
