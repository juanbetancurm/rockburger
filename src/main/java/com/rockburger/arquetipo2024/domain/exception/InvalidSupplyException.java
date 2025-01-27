package com.rockburger.arquetipo2024.domain.exception;

public class InvalidSupplyException extends RuntimeException {
    public InvalidSupplyException(String message) {
        super(message);
    }
}