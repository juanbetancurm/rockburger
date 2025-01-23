package com.rockburger.arquetipo2024.domain.model;

public class AuthenticationModel {
    private final String email;
    private final String password;

    public AuthenticationModel(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
