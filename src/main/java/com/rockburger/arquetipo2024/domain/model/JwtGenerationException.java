package com.rockburger.arquetipo2024.domain.model;

public class JwtGenerationException extends RuntimeException {
    public JwtGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}