package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.ArticleModel;

import java.util.List;

/**
 * Service port interface for Article domain operations.
 * Defines the business logic operations available for articles.
 */
public interface IArticleServicePort {

    /**
     * Creates a new article with the provided data.
     * Validates business rules like category count, brand existence, etc.
     *
     * @param articleModel the article data to create
     * @return the created article with generated ID
     * @throws com.rockburger.burgermain.domain.exception.BlankFieldException if required fields are missing
     * @throws com.rockburger.burgermain.domain.exception.InvalidCategoryCountException if category count is invalid
     * @throws com.rockburger.burgermain.domain.exception.NameAlreadyExistsException if article name already exists
     */
    ArticleModel createNewArticle(ArticleModel articleModel);

    /**
     * Retrieves a paginated and sorted list of articles.
     *
     * @param sortBy the field to sort by (e.g., "name", "price", "quantity")
     * @param sortOrder the sort direction ("asc" or "desc")
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @return list of articles matching the criteria
     */
    List<ArticleModel> listArticles(String sortBy, String sortOrder, int page, int size);

    /**
     * Retrieves a specific article by its ID.
     *
     * @param articleId the ID of the article to retrieve
     * @return the article with the specified ID
     * @throws com.rockburger.burgermain.domain.exception.ElementNotFoundException if article not found
     */
    ArticleModel getArticleById(Long articleId);

    /**
     * Updates an existing article with new data.
     * Validates business rules and ensures the article exists.
     *
     * @param articleModel the article data to update (must include ID)
     * @return the updated article
     * @throws com.rockburger.burgermain.domain.exception.ElementNotFoundException if article not found
     * @throws com.rockburger.burgermain.domain.exception.BlankFieldException if required fields are missing
     * @throws com.rockburger.burgermain.domain.exception.InvalidCategoryCountException if category count is invalid
     */
    ArticleModel updateArticle(ArticleModel articleModel);

    /**
     * Deletes an article by its ID.
     * Validates that the article exists and can be safely deleted.
     *
     * @param articleId the ID of the article to delete
     * @throws com.rockburger.burgermain.domain.exception.ElementNotFoundException if article not found
     * @throws com.rockburger.burgermain.domain.exception.ArticleInUseException if article cannot be deleted due to dependencies
     */
    void deleteArticle(Long articleId);

    /**
     * Checks if an article exists by ID.
     *
     * @param articleId the ID to check
     * @return true if the article exists, false otherwise
     */
    boolean existsById(Long articleId);

    /**
     * Checks if an article with the given name already exists.
     * Used for validation during create/update operations.
     *
     * @param name the article name to check
     * @return true if an article with this name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Checks if an article with the given name exists, excluding a specific ID.
     * Used for validation during update operations.
     *
     * @param name the article name to check
     * @param excludeId the article ID to exclude from the check
     * @return true if another article with this name exists, false otherwise
     */
    boolean existsByNameAndIdNot(String name, Long excludeId);
}