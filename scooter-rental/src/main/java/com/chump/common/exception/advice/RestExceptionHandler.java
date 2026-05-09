package com.chump.common.exception.advice;

import com.chump.common.dto.response.ErrorResponse;
import com.chump.common.exception.NoSuchEntityException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolationException;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    // ------------ ОБРАБОТКА БАЗОВЫХ ИСКЛЮЧЕНИЙ ------------
    // Spring оборачивает исключения Jackson в это исключение
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception
    ) {
        String details = getHttpMessageNotReadableDetails(exception.getCause());

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(400)
                .error("Parse exception")
                .message("Failed to parse your request. Check all fields are correct")
                .details(Collections.singletonList(details))
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported() {
        return new ResponseEntity<>(ErrorResponse.builder()
                .status(405)
                .error("Method not allowed")
                .message("This method cannot be used for this endpoint")
                .build(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied() {
        return new ResponseEntity<>(ErrorResponse.builder()
                .status(403)
                .error("Access denied")
                .message("You don't have required permissions")
                .build(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials() {
        return new ResponseEntity<>(ErrorResponse.builder()
                .status(401)
                .error("Bad credentials")
                .message("Check password and username are correct")
                .build(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException exception
    ) {
        String error = "Authentication Exception";
        String message = "Error while authenticating has occurred";

        // Проверка на неправильный username, выдаем одно и то же сообщение
        // позволяет избежать information disclosure
        if (exception.getCause() instanceof NoSuchEntityException) {
            error = "Bad credentials";
            message = "Check password and username are correct";
        }

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(401)
                .error(error)
                .message(message)
                .build(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleSimpleTypeValidation(
            ConstraintViolationException exception
    ) {
        List<String> details = exception
                .getConstraintViolations()
                .stream()
                .map(error -> error.getPropertyPath() + ": " + error.getMessage())
                .toList();


        return new ResponseEntity<>(ErrorResponse.builder()
                .status(400)
                .error("Validation exception")
                .message("Some of path variables or params failed to validate")
                .details(details)
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(
            MethodArgumentNotValidException exception
    ) {
        List<String> details = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(400)
                .error("Validation exception")
                .message("Some of fields failed to validate")
                .details(details)
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        String details = "Wrong value for parameter '" + exception.getParameter().getParameterName() + "': "
                + exception.getValue();

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(400)
                .error("Type mismatch")
                .message("Some of given parameters are of the wrong type")
                .details(Collections.singletonList(details))
                .build(), HttpStatus.BAD_REQUEST);
    }

    private @Nullable String getHttpMessageNotReadableDetails(@Nullable Throwable cause) {
        return cause == null ? null :
                cause instanceof InvalidFormatException ?
                        "Invalid value for field: " + ((InvalidFormatException) cause)
                                .getPath().get(0).getFieldName() :
                        cause instanceof MismatchedInputException ?
                                "Invalid type for field or field is excessive: " + ((MismatchedInputException) cause)
                                        .getPath().get(0).getFieldName() :
                                cause instanceof JsonParseException ?
                                        "Request JSON body is malformed" : null;
    }

    // ------------ ОБРАБОТКА КАСТОМНЫХ ИСКЛЮЧЕНИЙ ------------
    // TODO заменить
//    @ExceptionHandler(Exception.class)
//    private ResponseEntity<ErrorResponse> handleAny(
//            Exception exception
//    ) {
//        return new ResponseEntity<>(ErrorResponse.builder()
//                .status(500)
//                .error(exception.getClass().toString())
//                .message(exception.getMessage())
//                .details(Collections.singletonList("Temp custom exception handler"))
//                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
//    }
}
