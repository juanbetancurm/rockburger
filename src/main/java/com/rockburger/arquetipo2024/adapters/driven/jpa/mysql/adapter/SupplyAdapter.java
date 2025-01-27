package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.adapter;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.ArticleEntity;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.SupplyEntity;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.exception.ElementNotFoundException;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper.ISupplyEntityMapper;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.IArticleRepository;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.ISupplyRepository;
import com.rockburger.arquetipo2024.domain.exception.ConcurrentModificationException;
import com.rockburger.arquetipo2024.domain.model.SupplyModel;
import com.rockburger.arquetipo2024.domain.spi.ISupplyPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplyAdapter implements ISupplyPersistencePort {
    private static final Logger logger = LoggerFactory.getLogger(SupplyAdapter.class);

    private final ISupplyRepository supplyRepository;
    private final ISupplyEntityMapper supplyEntityMapper;
    private final IArticleRepository articleRepository;  // Add this

    public SupplyAdapter(ISupplyRepository supplyRepository,
                         ISupplyEntityMapper supplyEntityMapper,
                         IArticleRepository articleRepository) {
        this.supplyRepository = supplyRepository;
        this.supplyEntityMapper = supplyEntityMapper;
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional
    public SupplyModel saveSupply(SupplyModel supplyModel) {
        try {
            logger.info("Saving supply for article ID: {}", supplyModel.getArticle().getId());
            SupplyEntity supplyEntity = supplyEntityMapper.toEntity(supplyModel);
            SupplyEntity savedEntity = supplyRepository.save(supplyEntity);
            return supplyEntityMapper.toModel(savedEntity);
        } catch (Exception e) {
            logger.error("Error saving supply: ", e);
            throw new RuntimeException("Failed to save supply", e);
        }
    }

    @Override
    @Transactional
    public void updateArticleQuantity(Long articleId, int quantityToAdd) {
        try {
            logger.info("Updating quantity for article ID: {} by {}", articleId, quantityToAdd);

            ArticleEntity article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new ElementNotFoundException());

            // Update quantity
            article.setQuantity(article.getQuantity() + quantityToAdd);

            // Save will trigger version check automatically
            articleRepository.save(article);

        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("Concurrent modification detected when updating article quantity", e);
            throw new ConcurrentModificationException("Concurrent modification detected");
        } catch (Exception e) {
            logger.error("Error updating article quantity: ", e);
            throw new RuntimeException("Failed to update article quantity", e);
        }
    }
}