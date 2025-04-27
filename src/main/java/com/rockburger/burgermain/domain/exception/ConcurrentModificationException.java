package com.rockburger.burgermain.domain.exception;

public class ConcurrentModificationException extends RuntimeException {
    public ConcurrentModificationException(String message) {
        super(message);
    }
}