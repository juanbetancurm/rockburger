package com.rockburger.burgermain.domain.exception;
public class InvalidDocumentFormatException extends RuntimeException {
    public InvalidDocumentFormatException(String message) {
        super(message);
    }
}