package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.BrandModel;

import java.util.List;

/**
 * Service port interface for Brand domain operations.
 * Defines the business logic operations available for brands.
 */
public interface IBrandServicePort {

    /**
     * Creates a new brand with the provided data.
     * Validates business rules like name uniqueness.
     *
     * @param brandModel the brand data to create
     * @return the created brand with generated ID
     * @throws com.rockburger.burgermain.domain.exception.BlankFieldException if required fields are missing
     * @throws com.rockburger.burgermain.domain.exception.NameAlreadyExistsException if brand name already exists
     */
    BrandModel createBrand(BrandModel brandModel);

    /**
     * Retrieves a paginated and sorted list of brands.
     *
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @param sortBy the field to sort by (e.g., "name", "description")
     * @param asc true for ascending order, false for descending
     * @return list of brands matching the criteria
     */
    List<BrandModel> getBrandsWithPagination(int page, int size, String sortBy, boolean asc);

    /**
     * Retrieves a specific brand by its ID.
     *
     * @param brandId the ID of the brand to retrieve
     * @return the brand with the specified ID
     * @throws com.rockburger.burgermain.domain.exception.ElementNotFoundException if brand not found
     */
    BrandModel getBrandById(Long brandId);

    /**
     * Updates an existing brand with new data.
     * Validates business rules and ensures the brand exists.
     *
     * @param brandModel the brand data to update (must include ID)
     * @return the updated brand
     * @throws com.rockburger.burgermain.domain.exception.ElementNotFoundException if brand not found
     * @throws com.rockburger.burgermain.domain.exception.BlankFieldException if required fields are missing
     * @throws com.rockburger.burgermain.domain.exception.NameAlreadyExistsException if brand name already exists
     */
    BrandModel updateBrand(BrandModel brandModel);

    /**
     * Deletes a brand by its ID.
     * Validates that the brand exists and can be safely deleted.
     *
     * @param brandId the ID of the brand to delete
     * @throws com.rockburger.burgermain.domain.exception.ElementNotFoundException if brand not found
     * @throws com.rockburger.burgermain.domain.exception.BrandInUseException if brand cannot be deleted due to dependencies
     */
    void deleteBrand(Long brandId);

    /**
     * Checks if a brand exists by ID.
     *
     * @param brandId the ID to check
     * @return true if the brand exists, false otherwise
     */
    boolean existsById(Long brandId);

    /**
     * Checks if a brand with the given name already exists.
     * Used for validation during create/update operations.
     *
     * @param name the brand name to check
     * @return true if a brand with this name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Checks if a brand with the given name exists, excluding a specific ID.
     * Used for validation during update operations.
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
}