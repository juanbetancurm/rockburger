package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.exception;

public class NameAlreadyExistException extends RuntimeException{
    public NameAlreadyExistException(String name) {
        super(String.format("Category with name '%s' already exists", name));
    }
}
