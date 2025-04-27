package com.rockburger.burgermain.adapters.driven.jpa.mysql.repository;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.ArticleEntity;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.SupplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ISupplyRepository extends JpaRepository<SupplyEntity, Long> {
    Optional<ArticleEntity> findArticleById(Long articleId);
}