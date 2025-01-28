package com.rockburger.arquetipo2024.domain.spi;

import com.rockburger.arquetipo2024.domain.model.ClientModel;
import java.util.Optional;

public interface IClientPersistencePort {
    ClientModel save(ClientModel clientModel);
    boolean existsByEmail(String email);
    boolean existsByIdDocument(String idDocument);
    Optional<ClientModel> findByEmail(String email);
}