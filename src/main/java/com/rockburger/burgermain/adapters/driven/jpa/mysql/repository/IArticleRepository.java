package com.rockburger.burgermain.adapters.driven.jpa.mysql.repository;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.ArticleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface IArticleRepository extends JpaRepository<ArticleEntity, Long> {
    Optional<ArticleEntity> findByName(String name);
    Page<ArticleEntity> findAll(Pageable pageable);
}
