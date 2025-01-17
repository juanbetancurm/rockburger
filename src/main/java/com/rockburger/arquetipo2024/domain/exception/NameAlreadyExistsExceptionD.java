package com.rockburger.arquetipo2024.domain.exception;

public class NameAlreadyExistsExceptionD extends RuntimeException{
    public NameAlreadyExistsExceptionD (String name) {
        super(String.format("Name '%s' already exists", name));
    }
}