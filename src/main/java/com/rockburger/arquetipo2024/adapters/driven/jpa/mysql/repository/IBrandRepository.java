package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IBrandRepository extends JpaRepository<BrandEntity, Long> {
    Optional<BrandEntity> findByName(String name);

}
