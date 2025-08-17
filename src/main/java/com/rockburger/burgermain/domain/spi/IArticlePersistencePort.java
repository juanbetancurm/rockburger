package com.rockburger.burgermain.domain.spi;

import com.rockburger.burgermain.domain.model.ArticleModel;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port interface for Article domain operations.
 * Defines the data access operations required by the article domain.
 */
public interface IArticlePersistencePort {

    /**
     * Saves an article (create or update).
     * If the article has an ID, it will be updated; otherwise, it will be created.
     *
     * @param articleModel the article to save
     * @return the saved article with generated/updated ID
     */
    ArticleModel save(ArticleModel articleModel);

    /**
     * Retrieves a paginated and sorted list of articles.
     *
     * @param sortBy the field to sort by
     * @param sortOrder the sort direction ("asc" or "desc")
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @return list of articles
     */
    List<ArticleModel> listArticles(String sortBy, String sortOrder, int page, int size);

    /**
     * Finds an article by its ID.
     *
     * @param id the article ID
     * @return Optional containing the article if found, empty otherwise
     */
    Optional<ArticleModel> findById(Long id);

    /**
     * Updates an existing article.
     *
     * @param articleModel the article to update (must have an ID)
     * @return the updated article
     * @throws com.rockburger.burgermain.adapters.driven.jpa.mysql.exception.ElementNotFoundException if article not found
     */
    ArticleModel updateArticle(ArticleModel articleModel);

    /**
     * Deletes an article by its ID.
     *
     * @param id the ID of the article to delete
     * @throws com.rockburger.burgermain.adapters.driven.jpa.mysql.exception.ElementNotFoundException if article not found
     */
    void deleteArticle(Long id);

    /**
     * Checks if an article exists by ID.
     *
     * @param id the article ID to check
     * @return true if the article exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Finds an article by its name.
     *
     * @param name the article name
     * @return Optional containing the article if found, empty otherwise
     */
    Optional<ArticleModel> findByName(String name);

    /**
     * Checks if an article with the given name exists.
     *
     * @param name the article name to check
     * @return true if an article with this name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Checks if an article with the given name exists, excluding a specific ID.
     *
     * @param name the article name to check
     * @param excludeId the article ID to exclude from the check
     * @return true if another article with this name exists, false otherwise
     */
    boolean existsByNameAndIdNot(String name, Long excludeId);

    /**
     * Retrieves all articles without pagination.
     * Use with caution for large datasets.
     *
     * @return list of all articles
     */
    List<ArticleModel> findAll();

    /**
     * Checks if an article is being used in any orders or carts.
     * Used to prevent deletion of articles that are in use.
     *
     * @param articleId the article ID to check
     * @return true if the article is in use, false otherwise
     */
    boolean isArticleInUse(Long articleId);
}