package com.chump.common.exception;

public class NoSuchEntityException extends RuntimeException {

    public NoSuchEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchEntityException(String message) {
        super(message);
    }
}
