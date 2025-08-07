package com.rockburger.burgermain.domain.api.usecase;
import com.rockburger.burgermain.domain.api.IJwtServicePort;
import com.rockburger.burgermain.domain.exception.InvalidTokenException;
import com.rockburger.burgermain.domain.model.JwtModel;
import com.rockburger.burgermain.domain.model.UserModel;
import com.rockburger.burgermain.domain.spi.IJwtPersistencePort;
import com.rockburger.burgermain.domain.spi.IUserPersistencePort;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

public class JwtUseCase implements IJwtServicePort {
    private final IJwtPersistencePort jwtPersistencePort;
    private final IUserPersistencePort userPersistencePort;
    private final String jwtSecret;
    private final int jwtExpiration;

    public JwtUseCase(
            IJwtPersistencePort jwtPersistencePort,
            IUserPersistencePort userPersistencePort,
            String jwtSecret,
            int jwtExpiration) {
        this.jwtPersistencePort = jwtPersistencePort;
        this.userPersistencePort = userPersistencePort;
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    @Override
    public JwtModel generateToken(UserModel userModel) {
        String token = jwtPersistencePort.generateToken(userModel, jwtSecret, jwtExpiration);
        return new JwtModel(token, userModel.getId(), userModel.getEmail(), userModel.getRole());
    }

    @Override
    public UserModel validateAndGetUserFromToken(String token) {
        if (!jwtPersistencePort.isTokenValid(token, jwtSecret)) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        String mail = jwtPersistencePort.getUsernameFromToken(token, jwtSecret);
        return userPersistencePort.findByEmail(mail)
                .orElseThrow(() -> new InvalidTokenException("User not found for token"));
    }
}
