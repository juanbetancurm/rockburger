package com.rockburger.burgermain.adapters.driven.jpa.mysql.repository;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.BrandEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Brand entity operations.
 * Extends JpaRepository for basic CRUD operations and adds custom query methods.
 */
@Repository
public interface IBrandRepository extends JpaRepository<BrandEntity, Long> {

    /**
     * Finds a brand by its name.
     *
     * @param name the brand name to search for
     * @return Optional containing the brand if found, empty otherwise
     */
    Optional<BrandEntity> findByName(String name);

    /**
     * Finds all brands with pagination support.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated result of brands
     */
    Page<BrandEntity> findAll(Pageable pageable);

    /**
     * Checks if a brand exists with the given name.
     *
     * @param name the brand name to check
     * @return true if a brand with this name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Checks if a brand exists with the given name, excluding a specific ID.
     * Useful for update operations to check for name uniqueness.
     *
     * @param name the brand name to check
     * @param excludeId the brand ID to exclude from the search
     * @return true if another brand with this name exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BrandEntity b WHERE b.name = :name AND b.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * Finds brands by name containing a specific substring (case-insensitive).
     *
     * @param name the substring to search for in brand names
     * @return list of brands whose names contain the substring
     */
    @Query("SELECT b FROM BrandEntity b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    java.util.List<BrandEntity> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Finds brands by description containing a specific substring (case-insensitive).
     *
     * @param description the substring to search for in brand descriptions
     * @return list of brands whose descriptions contain the substring
     */
    @Query("SELECT b FROM BrandEntity b WHERE LOWER(b.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    java.util.List<BrandEntity> findByDescriptionContainingIgnoreCase(@Param("description") String description);

    /**
     * Counts the total number of brands.
     *
     * @return total count of brands
     */
    @Override
    long count();

    /**
     * Finds all brands ordered by name in ascending order.
     *
     * @return list of all brands ordered by name
     */
    @Query("SELECT b FROM BrandEntity b ORDER BY b.name ASC")
    java.util.List<BrandEntity> findAllOrderByNameAsc();

    /**
     * Finds all brands ordered by creation date in descending order.
     * Note: This assumes BrandEntity has a createdAt field
     *
     * @return list of all brands ordered by creation date (newest first)
     */
    @Query("SELECT b FROM BrandEntity b ORDER BY b.id DESC")
    java.util.List<BrandEntity> findAllOrderByIdDesc();
}