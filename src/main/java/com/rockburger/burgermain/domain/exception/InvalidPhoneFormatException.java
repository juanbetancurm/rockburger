package com.rockburger.burgermain.domain.exception;

public class InvalidPhoneFormatException extends RuntimeException {
    public InvalidPhoneFormatException(String message) {
        super(message);
    }
}