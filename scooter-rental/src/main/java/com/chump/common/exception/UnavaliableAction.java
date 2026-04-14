package com.chump.common.exception;

public class UnavaliableAction extends RuntimeException {

    public UnavaliableAction(String message) {
        super(message);
    }

    public UnavaliableAction(String message, Throwable cause) {
        super(message, cause);
    }
}
