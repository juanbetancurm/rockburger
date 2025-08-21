package com.rockburger.burgermain.domain.spi;

import com.rockburger.burgermain.domain.model.UserModel;

public interface IJwtPersistencePort {
    String generateToken(UserModel userModel, String secret, int expiration);
    String getUsernameFromToken(String token, String secret);
    String getRoleFromToken(String token, String secret);
    boolean isTokenValid(String token, String secret);

    // New methods for enhanced token management
    String generateRefreshToken(UserModel userModel, String secret, int expiration);
    long getTokenExpirationTime(String token, String secret);
    boolean isTokenExpired(String token, String secret);
    String getTokenType(String token, String secret); // "access" or "refresh"

    // Methods for token claims
    String getClaimFromToken(String token, String secret, String claimName);
    Long getUserIdFromToken(String token, String secret);
}