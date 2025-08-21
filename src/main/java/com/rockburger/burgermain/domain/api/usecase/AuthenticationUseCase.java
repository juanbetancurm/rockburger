package com.rockburger.burgermain.domain.api.usecase;

import com.rockburger.burgermain.domain.api.IAuthenticationServicePort;
import com.rockburger.burgermain.domain.api.IJwtServicePort;
import com.rockburger.burgermain.domain.exception.InvalidCredentialsException;
import com.rockburger.burgermain.domain.exception.NotFoundException;
import com.rockburger.burgermain.domain.model.JwtModel;
import com.rockburger.burgermain.domain.model.UserModel;
import com.rockburger.burgermain.domain.spi.IPasswordEncryptionPort;
import com.rockburger.burgermain.domain.spi.IUserPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationUseCase implements IAuthenticationServicePort {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationUseCase.class);

    private final IUserPersistencePort userPersistencePort;
    private final IJwtServicePort jwtServicePort;
    private final IPasswordEncryptionPort passwordEncryptionPort;

    public AuthenticationUseCase(
            IUserPersistencePort userPersistencePort,
            IJwtServicePort jwtServicePort,
            IPasswordEncryptionPort passwordEncryptionPort) {
        this.userPersistencePort = userPersistencePort;
        this.jwtServicePort = jwtServicePort;
        this.passwordEncryptionPort = passwordEncryptionPort;
    }

    @Override
    public JwtModel authenticate(String email, String password) {
        logger.debug("Authenticating user with email: {}", email);

        UserModel user = authenticateAndGetUser(email, password);
        JwtModel jwtModel = jwtServicePort.generateToken(user);

        logger.debug("Authentication successful for user: {}", email);
        return jwtModel;
    }

    @Override
    public UserModel authenticateAndGetUser(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Authentication attempt with empty email");
            throw new InvalidCredentialsException("Email is required");
        }

        if (password == null || password.trim().isEmpty()) {
            logger.warn("Authentication attempt with empty password for email: {}", email);
            throw new InvalidCredentialsException("Password is required");
        }

        // Find user by email
        UserModel user = userPersistencePort.findByEmail(email.trim())
                .orElseThrow(() -> {
                    logger.warn("Authentication failed - user not found: {}", email);
                    return new NotFoundException("User not found");
                });

        // Validate password using the correct method name
        if (!passwordEncryptionPort.matches(password, user.getPassword())) {
            logger.warn("Authentication failed - invalid password for user: {}", email);
            throw new InvalidCredentialsException("Invalid credentials");
        }

        logger.debug("User authentication successful: {}", email);
        return user;
    }

    @Override
    public boolean validateCredentials(String email, String password) {
        try {
            authenticateAndGetUser(email, password);
            return true;
        } catch (NotFoundException | InvalidCredentialsException e) {
            logger.debug("Credential validation failed for email: {} - {}", email, e.getMessage());
            return false;
        }
    }

    @Override
    public UserModel getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        return userPersistencePort.findByEmail(email.trim())
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", email);
                    return new NotFoundException("User not found with email: " + email);
                });
    }
}