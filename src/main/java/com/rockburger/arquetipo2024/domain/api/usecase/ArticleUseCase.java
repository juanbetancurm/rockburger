package com.rockburger.arquetipo2024.domain.api.usecase;


import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.exception.ElementNotFoundException;
import com.rockburger.arquetipo2024.domain.api.IArticleServicePort;
import com.rockburger.arquetipo2024.domain.api.IBrandServicePort;
import com.rockburger.arquetipo2024.domain.api.ICategoryServicePort;
import com.rockburger.arquetipo2024.domain.exception.BlankFieldException;
import com.rockburger.arquetipo2024.domain.exception.InvalidCategoryCountException;
import com.rockburger.arquetipo2024.domain.exception.InvalidParameterException;
import com.rockburger.arquetipo2024.domain.model.ArticleModel;

import com.rockburger.arquetipo2024.domain.model.BrandModel;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import com.rockburger.arquetipo2024.domain.spi.IArticlePersistencePort;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArticleUseCase implements IArticleServicePort {
    private static final Logger logger = LoggerFactory.getLogger(ArticleUseCase.class);
    private final IArticlePersistencePort articlePersistencePort;
    private final ICategoryServicePort categoryServicePort;
    private final IBrandServicePort brandServicePort;

    public ArticleUseCase(IArticlePersistencePort articlePersistencePort, ICategoryServicePort categoryServicePort, IBrandServicePort brandServicePort) {
        this.articlePersistencePort = articlePersistencePort;
        this.categoryServicePort = categoryServicePort;
        this.brandServicePort = brandServicePort;
    }

    @Override
    public ArticleModel createNewArticle(ArticleModel articleModel) {
        logger.info("Received ArticleModel in ArticleUseCase1: {}", articleModel);
        Long brandId = articleModel.getBrandId();
        logger.info("Received Brand in ArticleUseCase: {}", articleModel.getBrandId());
        if (brandId == null) {
            throw new BlankFieldException("Brand ID Cannot Be Null");
        }

        BrandModel brandModel = brandServicePort.getBrandById(brandId);
        if (brandModel == null) {
            throw new IllegalArgumentException("Invalid brand ID: " + brandId);
        }
        articleModel.setBrand(brandModel); // Set the brand model

        Set<Long> categoryIds = articleModel.getCategoryIds();
        logger.info("Extracted Category IDs: {}", categoryIds);

        if (categoryIds == null || categoryIds.isEmpty() || categoryIds.size() > 3) {
            throw new InvalidCategoryCountException("Article must have between 1 and 3 categories");
        }
        Set<Long> uniqueCategoryIds = new HashSet<>(categoryIds);
        if (uniqueCategoryIds.size() != categoryIds.size()) {
            throw new IllegalArgumentException("Duplicate categories are not allowed, categories should be different");
        }

        Set<CategoryModel> validCategories = new HashSet<>();
        for (Long categoryId : categoryIds) {
            CategoryModel categoryModel = categoryServicePort.getCategoryById(categoryId);
            validCategories.add(categoryModel);
        }

        logger.info("Categories validated and added: {}", validCategories);
        articleModel.setCategories(validCategories);

        if (validCategories.isEmpty() || validCategories.size() > 3) {
            throw new InvalidCategoryCountException("Article must have between 1 and 3 categories");
        }
        articleModel.setCategories(validCategories);

        ArticleModel savedArticle = articlePersistencePort.save(articleModel);
        logger.info("ArticleModel saved: {}", savedArticle);

        return savedArticle;
    }

    public List<ArticleModel> listArticles(String sortBy, String sortOrder, int page, int size) {

        if (page < 0) {
            throw new InvalidParameterException("Page number cannot be negative.");
        }
        if (size <= 0) {
            throw new InvalidParameterException("Page size must be greater than zero.");
        }
        if (sortBy == null || sortBy.trim().isEmpty()) {
            throw new InvalidParameterException("SortBy field must not be null or empty.");
        }
        return articlePersistencePort.listArticles(sortBy, sortOrder, page, size);

    }


    @Override
    public ArticleModel getArticleById(Long articleId) {
        logger.info("Getting article by ID: {}", articleId);

        if (articleId == null) {
            throw new BlankFieldException("Article ID cannot be null");
        }

        return articlePersistencePort.findById(articleId)
                .orElseThrow(() -> {
                    logger.error("Article not found with ID: {}", articleId);
                    return new ElementNotFoundException();
                });
    }
}

