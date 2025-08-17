package com.rockburger.burgermain.domain.api.usecase;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.exception.ElementNotFoundException;
import com.rockburger.burgermain.domain.api.IArticleServicePort;
import com.rockburger.burgermain.domain.api.IBrandServicePort;
import com.rockburger.burgermain.domain.api.ICategoryServicePort;
import com.rockburger.burgermain.domain.exception.*;
import com.rockburger.burgermain.domain.model.ArticleModel;
import com.rockburger.burgermain.domain.model.BrandModel;
import com.rockburger.burgermain.domain.model.CategoryModel;
import com.rockburger.burgermain.domain.spi.IArticlePersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Use case implementation for Article domain operations.
 * Implements business logic and validation rules for article management.
 */
public class ArticleUseCase implements IArticleServicePort {
    private static final Logger logger = LoggerFactory.getLogger(ArticleUseCase.class);

    private final IArticlePersistencePort articlePersistencePort;
    private final ICategoryServicePort categoryServicePort;
    private final IBrandServicePort brandServicePort;

    public ArticleUseCase(IArticlePersistencePort articlePersistencePort,
                          ICategoryServicePort categoryServicePort,
                          IBrandServicePort brandServicePort) {
        this.articlePersistencePort = articlePersistencePort;
        this.categoryServicePort = categoryServicePort;
        this.brandServicePort = brandServicePort;
    }

    @Override
    public ArticleModel createNewArticle(ArticleModel articleModel) {
        logger.info("Creating new article: {}", articleModel.getName());

        // Validate required fields
        validateArticleFields(articleModel);

        // Validate and set brand
        validateAndSetBrand(articleModel);

        // Validate and set categories
        validateAndSetCategories(articleModel);

        // Check if article name already exists
        if (articlePersistencePort.existsByName(articleModel.getName())) {
            throw new NameAlreadyExistsException("Article with name '" + articleModel.getName() + "' already exists");
        }

        ArticleModel createdArticle = articlePersistencePort.save(articleModel);
        logger.info("Article created successfully with ID: {}", createdArticle.getId());
        return createdArticle;
    }

    @Override
    public List<ArticleModel> listArticles(String sortBy, String sortOrder, int page, int size) {
        logger.debug("Listing articles: sortBy={}, sortOrder={}, page={}, size={}", sortBy, sortOrder, page, size);

        // Validate parameters
        if (page < 0) {
            throw new InvalidParameterException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new InvalidParameterException("Page size must be greater than zero");
        }
        if (sortBy == null || sortBy.trim().isEmpty()) {
            throw new InvalidParameterException("Sort by field cannot be null or empty");
        }
        if (sortOrder == null || (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc"))) {
            throw new InvalidParameterException("Sort order must be 'asc' or 'desc'");
        }

        return articlePersistencePort.listArticles(sortBy, sortOrder, page, size);
    }

    @Override
    public ArticleModel getArticleById(Long articleId) {
        logger.debug("Getting article by ID: {}", articleId);

        if (articleId == null) {
            throw new InvalidParameterException("Article ID cannot be null");
        }

        return articlePersistencePort.findById(articleId)
                .orElseThrow(() -> new NotFoundException("Article with ID " + articleId + " not found"));
    }

    @Override
    public ArticleModel updateArticle(ArticleModel articleModel) {
        logger.info("Updating article ID: {}", articleModel.getId());

        // Validate ID is present
        if (articleModel.getId() == null) {
            throw new InvalidParameterException("Article ID is required for update");
        }

        // Check if article exists
        Optional<ArticleModel> existingArticleOpt = articlePersistencePort.findById(articleModel.getId());
        if (!existingArticleOpt.isPresent()) {
            throw new NotFoundException("Article with ID " + articleModel.getId() + " not found");
        }

        // Validate required fields
        validateArticleFields(articleModel);

        // Validate and set brand
        validateAndSetBrand(articleModel);

        // Validate and set categories
        validateAndSetCategories(articleModel);

        // Check if the new name already exists (excluding current article)
        if (articlePersistencePort.existsByNameAndIdNot(articleModel.getName(), articleModel.getId())) {
            throw new NameAlreadyExistsException("Article with name '" + articleModel.getName() + "' already exists");
        }

        ArticleModel updatedArticle = articlePersistencePort.updateArticle(articleModel);
        logger.info("Article updated successfully: {}", updatedArticle.getId());
        return updatedArticle;
    }

    @Override
    public void deleteArticle(Long articleId) {
        logger.info("Deleting article ID: {}", articleId);

        if (articleId == null) {
            throw new InvalidParameterException("Article ID cannot be null");
        }

        // Check if article exists
        if (!articlePersistencePort.existsById(articleId)) {
            throw new NotFoundException("Article with ID " + articleId + " not found");
        }

        // Check if article is in use (orders, carts, etc.)
        if (articlePersistencePort.isArticleInUse(articleId)) {
            throw new IllegalStateException("Cannot delete article that is currently in use");
        }

        articlePersistencePort.deleteArticle(articleId);
        logger.info("Article deleted successfully: {}", articleId);
    }

    @Override
    public boolean existsById(Long articleId) {
        if (articleId == null) {
            return false;
        }
        return articlePersistencePort.existsById(articleId);
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return articlePersistencePort.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return articlePersistencePort.existsByNameAndIdNot(name, excludeId);
    }

    /**
     * Validates that required article fields are present and valid.
     */
    private void validateArticleFields(ArticleModel articleModel) {
        if (articleModel == null) {
            throw new BlankFieldException("Article cannot be null");
        }

        if (articleModel.getName() == null || articleModel.getName().trim().isEmpty()) {
            throw new BlankFieldException("Article name cannot be blank");
        }

        if (articleModel.getDescription() == null || articleModel.getDescription().trim().isEmpty()) {
            throw new BlankFieldException("Article description cannot be blank");
        }

        if (articleModel.getPrice() < 0) {
            throw new InvalidParameterException("Article price must be greater than or equal to zero");
        }

        if (articleModel.getQuantity() < 0) {
            throw new InvalidParameterException("Article quantity must be greater than or equal to zero");
        }

        // Validate length constraints
        if (articleModel.getName().length() > 50) {
            throw new InvalidParameterException("Article name cannot exceed 50 characters");
        }

        if (articleModel.getDescription().length() > 90) {
            throw new InvalidParameterException("Article description cannot exceed 90 characters");
        }
    }

    /**
     * Validates and sets the brand for the article.
     */
    private void validateAndSetBrand(ArticleModel articleModel) {
        Long brandId = articleModel.getBrandId();
        logger.debug("Validating brand ID: {}", brandId);

        if (brandId == null) {
            throw new BlankFieldException("Brand ID cannot be null");
        }

        BrandModel brandModel = brandServicePort.getBrandById(brandId);
        if (brandModel == null) {
            throw new IllegalArgumentException("Invalid brand ID: " + brandId);
        }

        articleModel.setBrand(brandModel);
        logger.debug("Brand set successfully: {}", brandModel.getName());
    }

    /**
     * Validates and sets the categories for the article.
     */
    private void validateAndSetCategories(ArticleModel articleModel) {
        Set<Long> categoryIds = articleModel.getCategoryIds();
        logger.debug("Validating category IDs: {}", categoryIds);

        if (categoryIds == null || categoryIds.isEmpty() || categoryIds.size() > 3) {
            throw new InvalidCategoryCountException("Article must have between 1 and 3 categories");
        }

        Set<CategoryModel> categoryModels = new HashSet<>();
        for (Long categoryId : categoryIds) {
            try {
                CategoryModel categoryModel = categoryServicePort.getCategoryById(categoryId);
                categoryModels.add(categoryModel);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid category ID: " + categoryId, e);
            }
        }

        articleModel.setCategories(categoryModels);
        logger.debug("Categories set successfully: {}", categoryModels.size());
    }
}