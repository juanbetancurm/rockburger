package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.adapter;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.ClientEntity;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper.IClientEntityMapper;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.IClientRepository;
import com.rockburger.arquetipo2024.domain.model.ClientModel;
import com.rockburger.arquetipo2024.domain.spi.IClientPersistencePort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class ClientAdapter implements IClientPersistencePort {
    private static final Logger logger = LoggerFactory.getLogger(ClientAdapter.class);

    private final IClientRepository clientRepository;
    private final IClientEntityMapper clientEntityMapper;

    public ClientAdapter(IClientRepository clientRepository,
                         IClientEntityMapper clientEntityMapper) {
        this.clientRepository = clientRepository;
        this.clientEntityMapper = clientEntityMapper;
    }

    @Override
    public ClientModel save(ClientModel clientModel) {
        logger.info("Saving client in persistence layer: {}", clientModel);
        ClientEntity clientEntity = clientEntityMapper.toEntity(clientModel);
        ClientEntity savedEntity = clientRepository.save(clientEntity);
        ClientModel savedModel = clientEntityMapper.toModel(savedEntity);
        logger.info("Successfully saved client with ID: {}", savedModel.getId());
        return savedModel;
    }

    @Override
    public boolean existsByEmail(String email) {
        return clientRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByIdDocument(String idDocument) {
        return clientRepository.existsByIdDocument(idDocument);
    }

    @Override
    public Optional<ClientModel> findByEmail(String email) {
        logger.debug("Finding client by email: {}", email);
        return clientRepository.findByEmail(email)
                .map(clientEntityMapper::toModel);
    }
}