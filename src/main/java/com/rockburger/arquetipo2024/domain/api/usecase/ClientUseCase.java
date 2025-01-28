package com.rockburger.arquetipo2024.domain.api.usecase;

import com.rockburger.arquetipo2024.domain.api.IClientServicePort;
import com.rockburger.arquetipo2024.domain.exception.DuplicateUserException;
import com.rockburger.arquetipo2024.domain.model.ClientModel;
import com.rockburger.arquetipo2024.domain.spi.IPasswordEncryptionPort;
import com.rockburger.arquetipo2024.domain.spi.IClientPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientUseCase implements IClientServicePort {
    private static final Logger logger = LoggerFactory.getLogger(ClientUseCase.class);

    private final IClientPersistencePort clientPersistencePort;
    private final IPasswordEncryptionPort passwordEncryptionPort;

    public ClientUseCase(IClientPersistencePort clientPersistencePort,
                         IPasswordEncryptionPort passwordEncryptionPort) {
        this.clientPersistencePort = clientPersistencePort;
        this.passwordEncryptionPort = passwordEncryptionPort;
    }

    @Override
    public ClientModel createClient(ClientModel clientModel) {
        logger.info("Creating new client with email: {}", clientModel.getEmail());

        // Validate age
        clientModel.validateAge();

        // Validate unique constraints
        validateUniqueConstraints(clientModel);

        // Encrypt password
        String encryptedPassword = passwordEncryptionPort.encryptPassword(clientModel.getPassword());
        clientModel.setEncryptedPassword(encryptedPassword);

        // Set role (although it's already set in the model constructor)
        clientModel.setRole("client");

        // Save client
        ClientModel savedClient = clientPersistencePort.save(clientModel);
        logger.info("Successfully created client with ID: {}", savedClient.getId());

        return savedClient;
    }

    @Override
    public boolean validateCredentials(String email, String password) {
        logger.debug("Validating credentials for email: {}", email);
        return clientPersistencePort.findByEmail(email)
                .map(client -> passwordEncryptionPort.matches(password, client.getPassword()))
                .orElse(false);
    }

    private void validateUniqueConstraints(ClientModel client) {
        if (clientPersistencePort.existsByEmail(client.getEmail())) {
            logger.error("Email already exists: {}", client.getEmail());
            throw new DuplicateUserException("Email already exists");
        }

        if (clientPersistencePort.existsByIdDocument(client.getIdDocument())) {
            logger.error("ID Document already exists: {}", client.getIdDocument());
            throw new DuplicateUserException("ID Document already exists");
        }
    }
}