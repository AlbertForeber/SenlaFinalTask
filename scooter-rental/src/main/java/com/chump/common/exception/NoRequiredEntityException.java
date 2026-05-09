package com.chump.common.exception;

// Отличие от NoSuchEntityException - проблема отсутствия сущности вина сервера, а не
// неверного запроса
public class NoRequiredEntityException extends RuntimeException {

    public NoRequiredEntityException(String message) {
        super(message);
    }
}
