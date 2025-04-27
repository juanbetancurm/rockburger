package com.rockburger.burgermain.domain.exception;

public class InvalidCategoryCountException extends RuntimeException{
    public InvalidCategoryCountException(String message) {
        super(message);
    }
}
