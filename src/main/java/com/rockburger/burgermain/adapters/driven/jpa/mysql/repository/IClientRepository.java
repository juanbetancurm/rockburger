package com.rockburger.burgermain.adapters.driven.jpa.mysql.repository;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IClientRepository extends JpaRepository<ClientEntity, Long> {
    Optional<ClientEntity> findByEmail(String email);
    Optional<ClientEntity> findByIdDocument(String idDocument);
    boolean existsByEmail(String email);
    boolean existsByIdDocument(String idDocument);
}
