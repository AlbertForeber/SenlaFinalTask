package com.chump.common.exception;


import org.springframework.security.core.AuthenticationException;

// TODO
// import org.springframework.security.core.AuthenticationException;
// BadCredentials отловить вручную
public class AuthException extends AuthenticationException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
