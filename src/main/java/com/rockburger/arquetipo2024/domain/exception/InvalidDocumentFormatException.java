package com.rockburger.arquetipo2024.domain.exception;
public class InvalidDocumentFormatException extends RuntimeException {
    public InvalidDocumentFormatException(String message) {
        super(message);
    }
}