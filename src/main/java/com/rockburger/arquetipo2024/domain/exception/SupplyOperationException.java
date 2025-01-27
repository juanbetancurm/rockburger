package com.rockburger.arquetipo2024.domain.exception;

public class SupplyOperationException extends RuntimeException {
    public SupplyOperationException(String message) {
        super(message);
    }

    public SupplyOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}