package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByIdDocument(String idDocument);
    boolean existsByEmail(String email);
    boolean existsByIdDocument(String idDocument);
}
