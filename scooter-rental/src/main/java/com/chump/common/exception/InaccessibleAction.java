package com.chump.common.exception;

public class InaccessibleAction extends RuntimeException {

    public InaccessibleAction(String message) {
        super(message);
    }

    public InaccessibleAction(String message, Throwable cause) {
        super(message, cause);
    }
}
