package com.rockburger.burgermain.domain.spi;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.CategoryEntity;
import com.rockburger.burgermain.domain.model.CategoryModel;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port interface for Category domain operations.
 * Defines the data access operations required by the category domain.
 */
public interface ICategoryPersistencePort {

    /**
     * Creates a new category.
     *
     * @param categoryModel the category to create
     * @return the created category with generated ID
     */
    CategoryModel createCategory(CategoryModel categoryModel);

    /**
     * Finds a category by its name.
     *
     * @param name the category name
     * @return Optional containing the category if found, empty otherwise
     */
    Optional<CategoryModel> getCategoryByName(String name);

    /**
     * Retrieves a paginated and sorted list of categories.
     *
     * @param page the page number (0-based)
     * @param size the number of items per page
     * @param sortBy the field to sort by
     * @param asc true for ascending order, false for descending
     * @return list of categories
     */
    List<CategoryModel> getCategoriesWithPagination(int page, int size, String sortBy, boolean asc);

    /**
     * Finds a category by its ID.
     *
     * @param categoryId the category ID
     * @return Optional containing the category if found, empty otherwise
     */
    Optional<CategoryModel> getCategoryById(Long categoryId);

    /**
     * Retrieves all categories as entities.
     * Used for specific operations that need entity access.
     *
     * @return list of all category entities
     */
    List<CategoryEntity> getAllCategories();

    /**
     * Updates an existing category.
     *
     * @param categoryModel the category to update (must have an ID)
     * @return the updated category
     * @throws com.rockburger.burgermain.adapters.driven.jpa.mysql.exception.ElementNotFoundException if category not found
     */
    CategoryModel updateCategory(CategoryModel categoryModel);

    /**
     * Deletes a category by its ID.
     *
     * @param id the ID of the category to delete
     * @throws com.rockburger.burgermain.adapters.driven.jpa.mysql.exception.ElementNotFoundException if category not found
     */
    void deleteCategory(Long id);

    /**
     * Checks if a category exists by ID.
     *
     * @param id the category ID to check
     * @return true if the category exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Checks if a category with the given name exists.
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
}