package com.rockburger.burgermain.domain.spi;

import com.rockburger.burgermain.domain.model.BrandModel;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port interface for Brand domain operations.
 * Defines the data access operations required by the brand domain.
 */
public interface IBrandPersistencePort {

    /**
     * Creates a new brand.
     *
     * @param brandModel the brand to create
     * @return the created brand with generated ID
     */
    BrandModel createBrand(BrandModel brandModel);

    /**
     * Retrieves a paginated and sorted list of brands.
     *
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @param sortBy the field to sort by
     * @param asc true for ascending order, false for descending
     * @return list of brands
     */
    List<BrandModel> getBrandsWithPagination(int page, int size, String sortBy, boolean asc);

    /**
     * Finds a brand by its ID.
     *
     * @param brandId the brand ID
     * @return the brand if found, null otherwise
     */
    BrandModel getBrandById(Long brandId);

    /**
     * Finds a brand by its name.
     *
     * @param name the brand name
     * @return Optional containing the brand if found, empty otherwise
     */
    Optional<BrandModel> getBrandByName(String name);

    /**
     * Updates an existing brand.
     *
     * @param brandModel the brand to update (must have an ID)
     * @return the updated brand
     * @throws com.rockburger.burgermain.adapters.driven.jpa.mysql.exception.ElementNotFoundException if brand not found
     */
    BrandModel updateBrand(BrandModel brandModel);

    /**
     * Deletes a brand by its ID.
     *
     * @param id the ID of the brand to delete
     * @throws com.rockburger.burgermain.adapters.driven.jpa.mysql.exception.ElementNotFoundException if brand not found
     */
    void deleteBrand(Long id);

    /**
     * Checks if a brand exists by ID.
     *
     * @param id the brand ID to check
     * @return true if the brand exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Checks if a brand with the given name exists.
     *
     * @param name the brand name to check
     * @return true if a brand with this name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Checks if a brand with the given name exists, excluding a specific ID.
     *
     * @param name the brand name to check
     * @param excludeId the brand ID to exclude from the check
     * @return true if another brand with this name exists, false otherwise
     */
    boolean existsByNameAndIdNot(String name, Long excludeId);

    /**
     * Retrieves all brands without pagination.
     * Use with caution for large datasets.
     *
     * @return list of all brands
     */
    List<BrandModel> getAllBrands();

    /**
     * Checks if a brand is being used by any articles.
     * Used to prevent deletion of brands that are in use.
     *
     * @param brandId the brand ID to check
     * @return true if the brand is in use, false otherwise
     */
    boolean isBrandInUse(Long brandId);
}