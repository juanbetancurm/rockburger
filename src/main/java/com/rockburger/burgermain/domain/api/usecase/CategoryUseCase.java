package com.rockburger.burgermain.domain.api.usecase;

import com.rockburger.burgermain.domain.api.ICategoryServicePort;
import com.rockburger.burgermain.domain.exception.BlankFieldException;
import com.rockburger.burgermain.domain.exception.CategoryNotFoundException;
import com.rockburger.burgermain.domain.exception.InvalidParameterException;
import com.rockburger.burgermain.domain.exception.NameAlreadyExistsExceptionD;
import com.rockburger.burgermain.domain.model.CategoryModel;
import com.rockburger.burgermain.domain.spi.ICategoryPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Use case implementation for Category domain operations.
 * Implements business logic and validation rules for category management.
 */
public class CategoryUseCase implements ICategoryServicePort {
    private static final Logger logger = LoggerFactory.getLogger(CategoryUseCase.class);

    private final ICategoryPersistencePort categoryPersistencePort;

    public CategoryUseCase(ICategoryPersistencePort categoryPersistencePort) {
        this.categoryPersistencePort = categoryPersistencePort;
    }

    @Override
    public CategoryModel createCategory(CategoryModel categoryModel) {
        logger.info("Creating category: {}", categoryModel.getName());

        // Validate required fields
        validateCategoryFields(categoryModel);

        // Check if category name already exists
        Optional<CategoryModel> existingCategory = categoryPersistencePort.getCategoryByName(categoryModel.getName());
        if (existingCategory.isPresent()) {
            logger.warn("Category name already exists: {}", categoryModel.getName());
            throw new NameAlreadyExistsExceptionD(categoryModel.getName());
        }

        CategoryModel createdCategory = categoryPersistencePort.createCategory(categoryModel);
        logger.info("Category created successfully with ID: {}", createdCategory.getId());
        return createdCategory;
    }

    @Override
    public CategoryModel getCategoryById(Long categoryId) {
        logger.debug("Getting category by ID: {}", categoryId);

        if (categoryId == null) {
            throw new InvalidParameterException("Category ID cannot be null");
        }

        return categoryPersistencePort.getCategoryById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID " + categoryId + " not found."));
    }

    @Override
    public List<CategoryModel> getCategoriesWithPagination(int page, int size, String sortBy, boolean asc) {
        logger.debug("Getting categories with pagination: page={}, size={}, sortBy={}, asc={}", page, size, sortBy, asc);

        // Validate pagination parameters
        if (page < 0) {
            throw new InvalidParameterException("Page number cannot be negative.");
        }
        if (size <= 0) {
            throw new InvalidParameterException("Page size must be greater than zero.");
        }
        if (sortBy == null || sortBy.trim().isEmpty()) {
            throw new InvalidParameterException("SortBy field must not be null or empty.");
        }

        return categoryPersistencePort.getCategoriesWithPagination(page, size, sortBy, asc);
    }

    @Override
    public CategoryModel updateCategory(CategoryModel categoryModel) {
        logger.info("Updating category ID: {}", categoryModel.getId());

        // Validate ID is present
        if (categoryModel.getId() == null) {
            throw new InvalidParameterException("Category ID is required for update");
        }

        // Validate required fields
        validateCategoryFields(categoryModel);

        // Check if category exists
        Optional<CategoryModel> existingCategory = categoryPersistencePort.getCategoryById(categoryModel.getId());
        if (!existingCategory.isPresent()) {
            throw new CategoryNotFoundException("Category with ID " + categoryModel.getId() + " not found.");
        }

        // Check if the new name already exists (excluding current category)
        Optional<CategoryModel> categoryWithSameName = categoryPersistencePort.getCategoryByName(categoryModel.getName());
        if (categoryWithSameName.isPresent() && !categoryWithSameName.get().getId().equals(categoryModel.getId())) {
            logger.warn("Category name already exists: {}", categoryModel.getName());
            throw new NameAlreadyExistsExceptionD(categoryModel.getName());
        }

        CategoryModel updatedCategory = categoryPersistencePort.updateCategory(categoryModel);
        logger.info("Category updated successfully: {}", updatedCategory.getId());
        return updatedCategory;
    }

    @Override
    public void deleteCategory(Long categoryId) {
        logger.info("Deleting category ID: {}", categoryId);

        if (categoryId == null) {
            throw new InvalidParameterException("Category ID cannot be null");
        }

        // Check if category exists
        Optional<CategoryModel> existingCategory = categoryPersistencePort.getCategoryById(categoryId);
        if (!existingCategory.isPresent()) {
            throw new CategoryNotFoundException("Category with ID " + categoryId + " not found.");
        }

        // TODO: Add validation to check if category is in use by articles
        // This would prevent deletion of categories that have associated articles

        categoryPersistencePort.deleteCategory(categoryId);
        logger.info("Category deleted successfully: {}", categoryId);
    }

    @Override
    public boolean existsById(Long categoryId) {
        if (categoryId == null) {
            return false;
        }
        return categoryPersistencePort.getCategoryById(categoryId).isPresent();
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return categoryPersistencePort.getCategoryByName(name).isPresent();
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        Optional<CategoryModel> category = categoryPersistencePort.getCategoryByName(name);
        return category.isPresent() && !category.get().getId().equals(excludeId);
    }

    @Override
    public List<CategoryModel> getAllCategories() {
        logger.debug("Getting all categories");
        // Use a large page size to get all categories
        return categoryPersistencePort.getCategoriesWithPagination(0, 1000, "name", true);
    }

    /**
     * Validates that required category fields are present and valid.
     *
     * @param categoryModel the category to validate
     * @throws BlankFieldException if required fields are missing or empty
     */
    private void validateCategoryFields(CategoryModel categoryModel) {
        if (categoryModel == null) {
            throw new BlankFieldException("Category cannot be null");
        }

        if (categoryModel.getName() == null || categoryModel.getName().trim().isEmpty()) {
            throw new BlankFieldException("Category name cannot be blank");
        }

        if (categoryModel.getDescription() == null || categoryModel.getDescription().trim().isEmpty()) {
            throw new BlankFieldException("Category description cannot be blank");
        }

        // Validate length constraints
        if (categoryModel.getName().length() > 50) {
            throw new InvalidParameterException("Category name cannot exceed 50 characters");
        }

        if (categoryModel.getDescription().length() > 90) {
            throw new InvalidParameterException("Category description cannot exceed 90 characters");
        }
    }
}