package com.rockburger.arquetipo2024.domain.spi;

import com.rockburger.arquetipo2024.domain.model.UserModel;

public interface IJwtPersistencePort {
    String generateToken(UserModel userModel, String secret, int expiration);
    String getUsernameFromToken(String token, String secret);
    String getRoleFromToken(String token, String secret);
    boolean isTokenValid(String token, String secret);
}
