package com.rockburger.arquetipo2024.configuration.exceptionhandler;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }
}