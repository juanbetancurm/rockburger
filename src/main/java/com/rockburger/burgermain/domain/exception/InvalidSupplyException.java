package com.rockburger.burgermain.domain.exception;

public class InvalidSupplyException extends RuntimeException {
    public InvalidSupplyException(String message) {
        super(message);
    }
}