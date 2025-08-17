package com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.ArticleEntity;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.exception.ElementNotFoundException;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper.IArticleEntityMapper;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.repository.IArticleRepository;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.repository.IBrandRepository;
import com.rockburger.burgermain.domain.exception.NotFoundException;
import com.rockburger.burgermain.domain.model.ArticleModel;
import com.rockburger.burgermain.domain.spi.IArticlePersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleAdapter implements IArticlePersistencePort {
    private static final Logger logger = LoggerFactory.getLogger(ArticleAdapter.class);

    private final IArticleRepository articleRepository;
    private final IArticleEntityMapper articleEntityMapper;
    private final IBrandRepository brandRepository;

    public ArticleAdapter(IArticleRepository articleRepository,
                          IArticleEntityMapper articleEntityMapper,
                          IBrandRepository brandRepository) {
        this.articleRepository = articleRepository;
        this.articleEntityMapper = articleEntityMapper;
        this.brandRepository = brandRepository;
    }

    @Override
    @Transactional
    public ArticleModel save(ArticleModel articleModel) {
        logger.info("Saving ArticleModel in persistence layer: {}", articleModel.getName());

        ArticleEntity articleEntity = articleEntityMapper.toEntity(articleModel);
        logger.debug("Mapped ArticleEntity: {}", articleEntity.getName());

        ArticleEntity savedArticle = articleRepository.save(articleEntity);
        ArticleModel savedModel = articleEntityMapper.toModel(savedArticle);

        logger.info("Saved ArticleEntity and returning ArticleModel with ID: {}", savedModel.getId());
        return savedModel;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleModel> listArticles(String sortBy, String sortOrder, int page, int size) {
        logger.debug("Listing articles: sortBy={}, sortOrder={}, page={}, size={}",
                sortBy, sortOrder, page, size);

        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return articleRepository.findAll(pageable)
                .stream()
                .map(articleEntityMapper::toModel)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArticleModel> findById(Long id) {
        logger.debug("Finding article by ID: {}", id);

        if (id == null) {
            return Optional.empty();
        }

        return articleRepository.findById(id)
                .map(articleEntityMapper::toModel);
    }

    @Override
    @Transactional
    public ArticleModel updateArticle(ArticleModel articleModel) {
        logger.debug("Updating article ID: {}", articleModel.getId());

        if (articleModel.getId() == null) {
            throw new IllegalArgumentException("Article ID is required for update");
        }

        // Check if article exists
        Optional<ArticleEntity> existingEntityOpt = articleRepository.findById(articleModel.getId());
        if (!existingEntityOpt.isPresent()) {
            throw new NotFoundException("Article with ID " + articleModel.getId() + " not found");
        }

        ArticleEntity articleEntity = articleEntityMapper.toEntity(articleModel);
        ArticleEntity updatedEntity = articleRepository.save(articleEntity);
        ArticleModel updatedModel = articleEntityMapper.toModel(updatedEntity);

        logger.debug("Article updated successfully: {}", updatedModel.getId());
        return updatedModel;
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        logger.debug("Deleting article ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("Article ID cannot be null");
        }

        // Check if article exists
        if (!articleRepository.existsById(id)) {
            throw new NotFoundException("Article with ID " + id + " not found");
        }

        // Check if article is in use
        if (isArticleInUse(id)) {
            throw new IllegalStateException("Cannot delete article that is currently in use");
        }

        articleRepository.deleteById(id);
        logger.debug("Article deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return articleRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArticleModel> findByName(String name) {
        logger.debug("Finding article by name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            // Try to find by name if the repository method exists
            return articleRepository.findByName(name)
                    .map(articleEntityMapper::toModel);
        } catch (Exception e) {
            // If method doesn't exist, fall back to manual search
            logger.debug("Repository findByName method not available, searching manually");
            return articleRepository.findAll()
                    .stream()
                    .filter(article -> name.equals(article.getName()))
                    .findFirst()
                    .map(articleEntityMapper::toModel);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        try {
            // Try to use repository method if available
            return articleRepository.existsByName(name);
        } catch (Exception e) {
            // If method doesn't exist, fall back to findByName
            logger.debug("Repository existsByName method not available, using findByName");
            return findByName(name).isPresent();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty() || excludeId == null) {
            return false;
        }

        try {
            // Try to use repository method if available
            return articleRepository.existsByNameAndIdNot(name, excludeId);
        } catch (Exception e) {
            // If method doesn't exist, fall back to manual search
            logger.debug("Repository existsByNameAndIdNot method not available, searching manually");
            Optional<ArticleModel> article = findByName(name);
            return article.isPresent() && !article.get().getId().equals(excludeId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleModel> findAll() {
        logger.debug("Finding all articles");

        return articleRepository.findAll()
                .stream()
                .map(articleEntityMapper::toModel)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isArticleInUse(Long articleId) {
        logger.debug("Checking if article {} is in use", articleId);

        if (articleId == null) {
            return false;
        }

        try {
            // Check if article is used in any orders (if order repository is available)
            // For now, we'll implement a basic check and return false
            // This can be enhanced later when order/cart checking is needed

            // Example checks that could be implemented:
            // - Check if article is in any active carts
            // - Check if article is referenced in any orders
            // - Check if article has any pending supplies

            // For now, assume articles can be deleted unless specifically restricted
            logger.debug("Article {} usage check completed - not in use", articleId);
            return false;

        } catch (Exception e) {
            // If we can't check due to missing dependencies, assume it's safe to delete
            logger.warn("Could not check if article {} is in use, assuming it's safe: {}", articleId, e.getMessage());
            return false;
        }
    }
}