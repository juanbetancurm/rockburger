package com.rockburger.burgermain.adapters.driven.jpa.mysql.repository;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Category entity operations.
 * Extends JpaRepository for basic CRUD operations and adds custom query methods.
 */
@Repository
public interface ICategoryRepository extends JpaRepository<CategoryEntity, Long> {

    /**
     * Finds a category by its name.
     *
     * @param name the category name to search for
     * @return Optional containing the category if found, empty otherwise
     */
    Optional<CategoryEntity> findByName(String name);

    /**
     * Finds all categories with pagination support.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated result of categories
     */
    Page<CategoryEntity> findAll(Pageable pageable);

    /**
     * Checks if a category exists with the given name.
     *
     * @param name the category name to check
     * @return true if a category with this name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Checks if a category exists with the given name, excluding a specific ID.
     * Useful for update operations to check for name uniqueness.
     *
     * @param name the category name to check
     * @param excludeId the category ID to exclude from the search
     * @return true if another category with this name exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CategoryEntity c WHERE c.name = :name AND c.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * Finds categories by name containing a specific substring (case-insensitive).
     *
     * @param name the substring to search for in category names
     * @return list of categories whose names contain the substring
     */
    @Query("SELECT c FROM CategoryEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    java.util.List<CategoryEntity> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Finds categories by description containing a specific substring (case-insensitive).
     *
     * @param description the substring to search for in category descriptions
     * @return list of categories whose descriptions contain the substring
     */
    @Query("SELECT c FROM CategoryEntity c WHERE LOWER(c.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    java.util.List<CategoryEntity> findByDescriptionContainingIgnoreCase(@Param("description") String description);

    /**
     * Counts the total number of categories.
     *
     * @return total count of categories
     */
    @Override
    long count();

    /**
     * Finds all categories ordered by name in ascending order.
     *
     * @return list of all categories ordered by name
     */
    @Query("SELECT c FROM CategoryEntity c ORDER BY c.name ASC")
    java.util.List<CategoryEntity> findAllOrderByNameAsc();

    /**
     * Finds all categories ordered by creation date in descending order.
     * Note: This assumes CategoryEntity has a createdAt field or uses ID as proxy
     *
     * @return list of all categories ordered by creation date (newest first)
     */
    @Query("SELECT c FROM CategoryEntity c ORDER BY c.id DESC")
    java.util.List<CategoryEntity> findAllOrderByIdDesc();

    /**
     * Checks if category exists by ID.
     * This method is inherited from JpaRepository but explicitly declared for clarity.
     *
     * @param id the category ID to check
     * @return true if category exists, false otherwise
     */
    @Override
    boolean existsById(Long id);
}