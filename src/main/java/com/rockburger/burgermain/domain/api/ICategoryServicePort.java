package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.CategoryModel;

import java.util.List;

/**
 * Service port interface for Category domain operations.
 * Defines the business logic operations available for categories.
 */
public interface ICategoryServicePort {

    /**
     * Creates a new category with the provided data.
     * Validates business rules like name uniqueness and required fields.
     *
     * @param categoryModel the category data to create
     * @return the created category with generated ID
     * @throws com.rockburger.burgermain.domain.exception.BlankFieldException if required fields are missing
     * @throws com.rockburger.burgermain.domain.exception.NameAlreadyExistsExceptionD if category name already exists
     */
    CategoryModel createCategory(CategoryModel categoryModel);

    /**
     * Retrieves a specific category by its ID.
     *
     * @param categoryId the ID of the category to retrieve
     * @return the category with the specified ID
     * @throws com.rockburger.burgermain.domain.exception.CategoryNotFoundException if category not found
     */
    CategoryModel getCategoryById(Long categoryId);

    /**
     * Retrieves a paginated and sorted list of categories.
     *
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @param sortBy the field to sort by (e.g., "name", "description")
     * @param asc true for ascending order, false for descending
     * @return list of categories matching the criteria
     * @throws com.rockburger.burgermain.domain.exception.InvalidParameterException if parameters are invalid
     */
    List<CategoryModel> getCategoriesWithPagination(int page, int size, String sortBy, boolean asc);

    /**
     * Updates an existing category with new data.
     * Validates business rules and ensures the category exists.
     *
     * @param categoryModel the category data to update (must include ID)
     * @return the updated category
     * @throws com.rockburger.burgermain.domain.exception.CategoryNotFoundException if category not found
     * @throws com.rockburger.burgermain.domain.exception.BlankFieldException if required fields are missing
     * @throws com.rockburger.burgermain.domain.exception.NameAlreadyExistsExceptionD if category name already exists
     */
    CategoryModel updateCategory(CategoryModel categoryModel);

    /**
     * Deletes a category by its ID.
     * Validates that the category exists and can be safely deleted.
     *
     * @param categoryId the ID of the category to delete
     * @throws com.rockburger.burgermain.domain.exception.CategoryNotFoundException if category not found
     * @throws com.rockburger.burgermain.domain.exception.CategoryInUseException if category cannot be deleted due to dependencies
     */
    void deleteCategory(Long categoryId);

    /**
     * Checks if a category exists by ID.
     *
     * @param categoryId the ID to check
     * @return true if the category exists, false otherwise
     */
    boolean existsById(Long categoryId);

    /**
     * Checks if a category with the given name already exists.
     * Used for validation during create/update operations.
     *
     * @param name the category name to check
     * @return true if a category with this name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Checks if a category with the given name exists, excluding a specific ID.
     * Used for validation during update operations.
     *
     * @param name the category name to check
     * @param excludeId the category ID to exclude from the check
     * @return true if another category with this name exists, false otherwise
     */
    boolean existsByNameAndIdNot(String name, Long excludeId);

    /**
     * Retrieves all categories without pagination.
     * Use with caution for large datasets.
     *
     * @return list of all categories
     */
    List<CategoryModel> getAllCategories();
}