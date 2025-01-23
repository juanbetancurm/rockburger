package com.rockburger.arquetipo2024.domain.model;

public class JwtModel {
    private final String token;
    private final Long userId;
    private final String email;
    private final String role;
    private final String tokenType;

    public JwtModel(String token, Long userId, String email, String role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.tokenType = "Bearer";
    }

    // Getters only for immutability
    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getTokenType() {
        return tokenType;
    }
}

