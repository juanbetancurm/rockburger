package com.rockburger.burgermain.domain.api.usecase;

import com.rockburger.burgermain.domain.api.IJwtServicePort;
import com.rockburger.burgermain.domain.exception.InvalidTokenException;
import com.rockburger.burgermain.domain.model.JwtModel;
import com.rockburger.burgermain.domain.model.UserModel;
import com.rockburger.burgermain.domain.spi.IJwtPersistencePort;
import com.rockburger.burgermain.domain.spi.IUserPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class JwtUseCase implements IJwtServicePort {
    private static final Logger logger = LoggerFactory.getLogger(JwtUseCase.class);

    private final IJwtPersistencePort jwtPersistencePort;
    private final IUserPersistencePort userPersistencePort;
    private final String jwtSecret;
    private final int jwtAccessTokenExpiration;
    private final int jwtRefreshTokenExpiration;

    // Constants for token expiration times
    private static final long FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000; // 5 minutes
    private static final long ACCESS_TOKEN_EXPIRATION = 60 * 60 * 1000; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

    public JwtUseCase(
            IJwtPersistencePort jwtPersistencePort,
            IUserPersistencePort userPersistencePort,
            String jwtSecret,
            int jwtAccessTokenExpiration,
            int jwtRefreshTokenExpiration) {
        this.jwtPersistencePort = jwtPersistencePort;
        this.userPersistencePort = userPersistencePort;
        this.jwtSecret = jwtSecret;
        this.jwtAccessTokenExpiration = jwtAccessTokenExpiration > 0 ? jwtAccessTokenExpiration : (int) ACCESS_TOKEN_EXPIRATION;
        this.jwtRefreshTokenExpiration = jwtRefreshTokenExpiration > 0 ? jwtRefreshTokenExpiration : (int) REFRESH_TOKEN_EXPIRATION;
    }

    @Override
    public JwtModel generateToken(UserModel userModel) {
        logger.debug("Generating access token for user: {}", userModel.getEmail());
        String token = jwtPersistencePort.generateToken(userModel, jwtSecret, jwtAccessTokenExpiration);
        return new JwtModel(token, userModel.getId(), userModel.getEmail(), userModel.getRole());
    }

    @Override
    public JwtTokenPair generateTokenPair(UserModel userModel) {
        logger.debug("Generating token pair for user: {}", userModel.getEmail());

        // Generate access token (short-lived)
        String accessToken = jwtPersistencePort.generateToken(userModel, jwtSecret, jwtAccessTokenExpiration);

        // Generate refresh token (long-lived)
        String refreshToken = jwtPersistencePort.generateRefreshToken(userModel, jwtSecret, jwtRefreshTokenExpiration);

        long currentTime = System.currentTimeMillis();

        return new JwtTokenPair(
                accessToken,
                refreshToken,
                currentTime + jwtAccessTokenExpiration,
                currentTime + jwtRefreshTokenExpiration,
                userModel.getEmail(),
                userModel.getRole()
        );
    }

    @Override
    public UserModel validateAndGetUserFromToken(String token) {
        logger.debug("Validating token and extracting user information");

        if (!jwtPersistencePort.isTokenValid(token, jwtSecret)) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        String email = jwtPersistencePort.getUsernameFromToken(token, jwtSecret);
        return userPersistencePort.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found for token"));
    }

    @Override
    public JwtModel refreshToken(String refreshToken) {
        logger.debug("Attempting to refresh token");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new InvalidTokenException("Refresh token is required");
        }

        try {
            // Validate refresh token
            if (!jwtPersistencePort.isTokenValid(refreshToken, jwtSecret)) {
                throw new InvalidTokenException("Invalid or expired refresh token");
            }

            // Extract user from refresh token
            String email = jwtPersistencePort.getUsernameFromToken(refreshToken, jwtSecret);
            UserModel user = userPersistencePort.findByEmail(email)
                    .orElseThrow(() -> new InvalidTokenException("User not found for refresh token"));

            // Generate new access token
            String newAccessToken = jwtPersistencePort.generateToken(user, jwtSecret, jwtAccessTokenExpiration);

            logger.debug("Successfully refreshed token for user: {}", email);
            return new JwtModel(newAccessToken, user.getId(), user.getEmail(), user.getRole());

        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage());
            throw new InvalidTokenException("Failed to refresh token: " + e.getMessage());
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            return !jwtPersistencePort.isTokenValid(token, jwtSecret);
        } catch (Exception e) {
            logger.debug("Token validation failed, considering it expired: {}", e.getMessage());
            return true;
        }
    }

    @Override
    public boolean isTokenExpiringSoon(String token) {
        try {
            long expirationTime = getTokenExpirationTime(token);
            long currentTime = System.currentTimeMillis();
            long timeUntilExpiration = expirationTime - currentTime;

            // Token is expiring soon if it expires within 5 minutes
            boolean expiringSoon = timeUntilExpiration <= FIVE_MINUTES_IN_MILLIS;

            if (expiringSoon) {
                logger.debug("Token expiring soon. Time until expiration: {} ms", timeUntilExpiration);
            }

            return expiringSoon;
        } catch (Exception e) {
            logger.debug("Could not determine token expiration time, considering it expiring soon: {}", e.getMessage());
            return true;
        }
    }

    @Override
    public String extractUserEmailFromToken(String token) {
        try {
            return jwtPersistencePort.getUsernameFromToken(token, jwtSecret);
        } catch (Exception e) {
            logger.error("Error extracting email from token: {}", e.getMessage());
            throw new InvalidTokenException("Could not extract user email from token");
        }
    }

    @Override
    public long getTokenExpirationTime(String token) {
        try {
            return jwtPersistencePort.getTokenExpirationTime(token, jwtSecret);
        } catch (Exception e) {
            logger.error("Error getting token expiration time: {}", e.getMessage());
            throw new InvalidTokenException("Could not get token expiration time");
        }
    }
}