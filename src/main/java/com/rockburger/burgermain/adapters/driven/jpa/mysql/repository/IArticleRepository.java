package com.rockburger.burgermain.adapters.driven.jpa.mysql.repository;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.ArticleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Article entity operations.
 * Extends JpaRepository for basic CRUD operations and adds custom query methods.
 */
@Repository
public interface IArticleRepository extends JpaRepository<ArticleEntity, Long> {

    /**
     * Finds an article by its name.
     *
     * @param name the article name to search for
     * @return Optional containing the article if found, empty otherwise
     */
    Optional<ArticleEntity> findByName(String name);

    /**
     * Finds all articles with pagination support.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated result of articles
     */
    Page<ArticleEntity> findAll(Pageable pageable);

    /**
     * Checks if an article exists with the given name.
     *
     * @param name the article name to check
     * @return true if an article with this name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Checks if an article exists with the given name, excluding a specific ID.
     * Useful for update operations to check for name uniqueness.
     *
     * @param name the article name to check
     * @param excludeId the article ID to exclude from the search
     * @return true if another article with this name exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM ArticleEntity a WHERE a.name = :name AND a.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * Checks if any articles are using the specified brand.
     * Used to prevent deletion of brands that are in use.
     *
     * @param brandId the brand ID to check
     * @return true if any articles use this brand, false otherwise
     */
    boolean existsByBrandId(Long brandId);

    /**
     * Finds all articles that belong to a specific brand.
     *
     * @param brandId the brand ID to search for
     * @return list of articles belonging to the brand
     */
    @Query("SELECT a FROM ArticleEntity a WHERE a.brand.id = :brandId")
    java.util.List<ArticleEntity> findByBrandId(@Param("brandId") Long brandId);

    /**
     * Finds all articles that contain a specific category.
     * Used to check if a category is in use by any articles.
     *
     * @param categoryId the category ID to search for
     * @return list of articles that contain this category
     */
    @Query("SELECT a FROM ArticleEntity a JOIN a.categories c WHERE c.id = :categoryId")
    java.util.List<ArticleEntity> findByCategoriesContaining(@Param("categoryId") Long categoryId);

    /**
     * Checks if any articles contain the specified category.
     * Used to prevent deletion of categories that are in use.
     *
     * @param categoryId the category ID to check
     * @return true if any articles contain this category, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM ArticleEntity a JOIN a.categories c WHERE c.id = :categoryId")
    boolean existsByCategoriesContaining(@Param("categoryId") Long categoryId);

    /**
     * Finds articles by name containing a specific substring (case-insensitive).
     *
     * @param name the substring to search for in article names
     * @return list of articles whose names contain the substring
     */
    @Query("SELECT a FROM ArticleEntity a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    java.util.List<ArticleEntity> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Finds articles with quantity below a specified threshold.
     * Useful for low stock alerts.
     *
     * @param threshold the quantity threshold
     * @return list of articles with quantity below the threshold
     */
    @Query("SELECT a FROM ArticleEntity a WHERE a.quantity < :threshold")
    java.util.List<ArticleEntity> findByQuantityLessThan(@Param("threshold") int threshold);

    /**
     * Finds articles within a price range.
     *
     * @param minPrice minimum price (inclusive)
     * @param maxPrice maximum price (inclusive)
     * @return list of articles within the price range
     */
    @Query("SELECT a FROM ArticleEntity a WHERE a.price BETWEEN :minPrice AND :maxPrice")
    java.util.List<ArticleEntity> findByPriceBetween(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice);
}