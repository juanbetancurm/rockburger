package com.rockburger.burgermain.domain.exception;

/**
 * Exception thrown when JWT token generation fails
 */
public class JwtGenerationException extends RuntimeException {

    public JwtGenerationException(String message) {
        super(message);
    }

    public JwtGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}