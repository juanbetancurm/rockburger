package com.rockburger.burgermain.domain.spi;

import com.rockburger.burgermain.domain.model.ClientModel;
import java.util.Optional;

public interface IClientPersistencePort {
    ClientModel save(ClientModel clientModel);
    boolean existsByEmail(String email);
    boolean existsByIdDocument(String idDocument);
    Optional<ClientModel> findByEmail(String email);
}