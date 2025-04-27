package com.rockburger.burgermain.domain.exception;

public class BlankFieldException extends RuntimeException{
    public BlankFieldException(String message) {
        super(message);
    }
}
