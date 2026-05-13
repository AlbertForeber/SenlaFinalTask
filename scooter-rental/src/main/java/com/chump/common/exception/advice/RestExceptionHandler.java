package com.chump.common.exception.advice;

import com.chump.common.dto.response.ErrorResponse;
import com.chump.common.exception.DataManipulationException;
import com.chump.common.exception.NoRequiredEntityException;
import com.chump.common.exception.NoSuchEntityException;
import com.chump.common.exception.UnavailableActionException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.List;

@Slf4j
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
    public ResponseEntity<ErrorResponse> handleAuthenticationException() {
        return new ResponseEntity<>(ErrorResponse.builder()
                .status(401)
                .error("Authentication Exception")
                .message("Error while authenticating has occurred")
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

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleArgumentTypeMismatch(
            HttpMediaTypeNotSupportedException exception
    ) {
        return new ResponseEntity<>(ErrorResponse.builder()
                .status(415)
                .error("Type not supported exception")
                .message("The only supported type is JSON")
                .build(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
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
    @ExceptionHandler(NoRequiredEntityException.class)
    private ResponseEntity<ErrorResponse> handleNoRequiredEntityException(
            NoRequiredEntityException exception
    ) {
        log.error("Required entity not found: {}", exception.getMessage());

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(500)
                .error("No required entity exception")
                .message("Some of required data was not found. Please contact support team")
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoSuchEntityException.class)
    private ResponseEntity<ErrorResponse> handleNoSuchEntityException(
            NoSuchEntityException exception
    ) {
        log.warn("Requested entity not found: {}", exception.getMessage());

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(400)
                .error("No such entity exception")
                .message("Requested data was not found")
                .details(Collections.singletonList(exception.getMessage()))
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnavailableActionException.class)
    private ResponseEntity<ErrorResponse> handleUnavailableActionException(
            UnavailableActionException exception
    ) {
        log.warn("Unavailable action tried to perform: {}", exception.getMessage());

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(400)
                .error("Unavailable action exception")
                .message("You are not able to perform such action")
                .details(Collections.singletonList(exception.getMessage()))
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataManipulationException.class)
    private ResponseEntity<ErrorResponse> handleDataManipulationException(
            DataManipulationException exception
    ) {
        log.error("Data manipulation exception occurred", exception);

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(500)
                .error("Data manipulation exception")
                .message("Something went wrong during processing your request, please try later")
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<ErrorResponse> handleAny(
            Exception exception
    ) {
        log.error("Unexpected error occurred", exception);

        return new ResponseEntity<>(ErrorResponse.builder()
                .status(500)
                .error("Internal server error")
                .message("Something went wrong, please try again later")
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
