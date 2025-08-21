package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.JwtModel;
import com.rockburger.burgermain.domain.model.UserModel;

public interface IJwtServicePort {
    JwtModel generateToken(UserModel userModel);
    UserModel validateAndGetUserFromToken(String token);

    // New methods for token refresh functionality
    JwtModel refreshToken(String refreshToken);
    boolean isTokenExpired(String token);
    boolean isTokenExpiringSoon(String token); // Check if token expires within 5 minutes
    String extractUserEmailFromToken(String token);
    long getTokenExpirationTime(String token);

    // Method to generate both access and refresh tokens
    JwtTokenPair generateTokenPair(UserModel userModel);

    // Inner class to hold token pair
    class JwtTokenPair {
        private final String accessToken;
        private final String refreshToken;
        private final long accessTokenExpiration;
        private final long refreshTokenExpiration;
        private final String userEmail;
        private final String userRole;

        public JwtTokenPair(String accessToken, String refreshToken,
                            long accessTokenExpiration, long refreshTokenExpiration,
                            String userEmail, String userRole) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.accessTokenExpiration = accessTokenExpiration;
            this.refreshTokenExpiration = refreshTokenExpiration;
            this.userEmail = userEmail;
            this.userRole = userRole;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public long getAccessTokenExpiration() {
            return accessTokenExpiration;
        }

        public long getRefreshTokenExpiration() {
            return refreshTokenExpiration;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public String getUserRole() {
            return userRole;
        }
    }
}