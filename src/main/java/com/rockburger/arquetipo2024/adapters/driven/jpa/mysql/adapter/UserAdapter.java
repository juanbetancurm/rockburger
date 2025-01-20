package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.adapter;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.UserEntity;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper.IUserEntityMapper;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.IUserRepository;
import com.rockburger.arquetipo2024.domain.model.UserModel;
import com.rockburger.arquetipo2024.domain.spi.IUserPersistencePort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class UserAdapter implements IUserPersistencePort {
    private static final Logger logger = LoggerFactory.getLogger(UserAdapter.class);

    private final IUserRepository userRepository;
    private final IUserEntityMapper userEntityMapper;

    public UserAdapter(IUserRepository userRepository,
                                IUserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEntityMapper = userEntityMapper;
    }

    @Override
    public UserModel save(UserModel userModel) {
        logger.info("Saving  user in persistence layer: {}", userModel);
        UserEntity userEntity = userEntityMapper.toEntity(userModel);
        UserEntity savedEntity = userRepository.save(userEntity);
        UserModel savedModel = userEntityMapper.toModel(savedEntity);
        logger.info("Successfully saved  user with ID: {}", savedModel.getId());
        return savedModel;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByIdDocument(String idDocument) {
        return userRepository.existsByIdDocument(idDocument);
    }

    @Override
    public Optional<UserModel> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .map(userEntityMapper::toModel);
    }
}