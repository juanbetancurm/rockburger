package com.rockburger.burgermain.domain.api.usecase;

import com.rockburger.burgermain.domain.api.IUserServicePort;
import com.rockburger.burgermain.domain.exception.DuplicateUserException;
import com.rockburger.burgermain.domain.model.UserModel;
import com.rockburger.burgermain.domain.spi.IPasswordPEncryptionPort;
import com.rockburger.burgermain.domain.spi.IUserPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UserUseCase implements IUserServicePort {
    private static final Logger logger = LoggerFactory.getLogger(UserUseCase.class);
    private final IUserPersistencePort userPersistencePort;
    private final IPasswordPEncryptionPort passwordEncryptionPort;

    public UserUseCase(IUserPersistencePort userPersistencePort,
                       IPasswordPEncryptionPort passwordEncryptionPort) {
        this.userPersistencePort = userPersistencePort;
        this.passwordEncryptionPort = passwordEncryptionPort;
    }

    @Override
    public UserModel createUser(UserModel userModel) {
        logger.info("Creating new  user with email: {}", userModel.getEmail());

        userModel.validateAge();
        validateUniqueConstraints(userModel);

        // Encrypt password and set role
        String encryptedPassword = passwordEncryptionPort.encryptPassword(userModel.getPassword());
        userModel.setEncryptedPassword(encryptedPassword);
        userModel.setRole("ROLE_auxiliar");

        UserModel savedUser = userPersistencePort.save(userModel);
        logger.info("Successfully created  user with ID: {}", savedUser.getId());

        return savedUser;
    }

    @Override
    public boolean validateCredentials(String email, String password) {
        logger.debug("Validating credentials for email: {}", email);

        return userPersistencePort.findByEmail(email)
                .map(user -> passwordEncryptionPort.matches(password, user.getPassword()))
                .orElse(false);
    }

    private void validateUniqueConstraints(UserModel user) {
        if (userPersistencePort.existsByEmail(user.getEmail())) {
            logger.error("Email already exists: {}", user.getEmail());
            throw new DuplicateUserException("Email already exists");
        }
        if (userPersistencePort.existsByIdDocument(user.getIdDocument())) {
            logger.error("ID Document already exists: {}", user.getIdDocument());
            throw new DuplicateUserException("ID Document already exists");
        }
    }
}