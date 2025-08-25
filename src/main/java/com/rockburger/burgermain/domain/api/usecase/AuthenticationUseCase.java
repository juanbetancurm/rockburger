package com.rockburger.burgermain.domain.api.usecase;


import com.rockburger.burgermain.domain.api.IAuthenticationServicePort;
import com.rockburger.burgermain.domain.exception.InvalidCredentialsException;
import com.rockburger.burgermain.domain.model.JwtModel;
import com.rockburger.burgermain.domain.model.UserModel;
import com.rockburger.burgermain.domain.api.IJwtServicePort;
import com.rockburger.burgermain.domain.spi.IUserPersistencePort;
import com.rockburger.burgermain.domain.spi.IPasswordPEncryptionPort;


public class AuthenticationUseCase implements IAuthenticationServicePort {
    private final IUserPersistencePort userPersistencePort;
    private final IJwtServicePort jwtServicePort;
    private final IPasswordPEncryptionPort passwordEncryptionPort;

    public AuthenticationUseCase(
            IUserPersistencePort userPersistencePort,
            IJwtServicePort jwtServicePort,
            IPasswordPEncryptionPort passwordEncryptionPort) {
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

