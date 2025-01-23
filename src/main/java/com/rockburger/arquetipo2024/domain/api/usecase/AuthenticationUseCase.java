package com.rockburger.arquetipo2024.domain.api.usecase;


import com.rockburger.arquetipo2024.domain.api.IAuthenticationServicePort;
import com.rockburger.arquetipo2024.domain.exception.InvalidCredentialsException;
import com.rockburger.arquetipo2024.domain.model.JwtModel;
import com.rockburger.arquetipo2024.domain.model.UserModel;
import com.rockburger.arquetipo2024.domain.api.IJwtServicePort;
import com.rockburger.arquetipo2024.domain.spi.IUserPersistencePort;
import com.rockburger.arquetipo2024.domain.spi.IPasswordEncryptionPort;


public class AuthenticationUseCase implements IAuthenticationServicePort {
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
        UserModel user = userPersistencePort.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncryptionPort.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return jwtServicePort.generateToken(user);
    }
}

