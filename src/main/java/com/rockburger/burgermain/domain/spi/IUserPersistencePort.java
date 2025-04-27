package com.rockburger.burgermain.domain.spi;

import com.rockburger.burgermain.domain.model.UserModel;

import java.util.Optional;

public interface IUserPersistencePort {
    UserModel save(UserModel userModel);
    boolean existsByEmail(String email);
    boolean existsByIdDocument(String idDocument);
    Optional<UserModel> findByEmail(String email);
}
