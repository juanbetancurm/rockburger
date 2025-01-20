package com.rockburger.arquetipo2024.domain.exception;

public class InvalidPhoneFormatException extends RuntimeException {
    public InvalidPhoneFormatException(String message) {
        super(message);
    }
}