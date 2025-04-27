package com.rockburger.burgermain.domain.exception;

public class NegativeNotAllowedException extends RuntimeException {
    public NegativeNotAllowedException(String message) {
        super(message);
    }
}
