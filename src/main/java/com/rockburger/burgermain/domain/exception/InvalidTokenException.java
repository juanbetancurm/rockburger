package com.rockburger.burgermain.domain.exception;

/**
 * Exception thrown when JWT token validation fails
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}