package com.rockburger.arquetipo2024.domain.api.usecase;

import com.rockburger.arquetipo2024.domain.api.ICategoryServicePort;
import com.rockburger.arquetipo2024.domain.exception.BlankFieldException;
import com.rockburger.arquetipo2024.domain.exception.CategoryNotFoundException;
import com.rockburger.arquetipo2024.domain.exception.InvalidParameterException;
import com.rockburger.arquetipo2024.domain.exception.NameAlreadyExistsExceptionD;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import com.rockburger.arquetipo2024.domain.spi.ICategoryPersistencePort;

import java.util.List;
import java.util.Optional;


public class CategoryUseCase implements ICategoryServicePort {
    private final ICategoryPersistencePort categoryPersistencePort;

    public CategoryUseCase(ICategoryPersistencePort categoryPersistencePort){
        this.categoryPersistencePort = categoryPersistencePort;
    }
    @Override
    public CategoryModel createCategory (CategoryModel categoryModel){
        Optional<CategoryModel> existingCategory = categoryPersistencePort.getCategoryByName(categoryModel.getName());
        if (categoryModel.getName() == null || categoryModel.getName().trim().isEmpty() ||
                categoryModel.getDescription() == null || categoryModel.getDescription().trim().isEmpty()) {
            throw new BlankFieldException("Field cannot be blank");
        }
        if (existingCategory.isPresent()) {
            throw new NameAlreadyExistsExceptionD(categoryModel.getName());
        }

        return categoryPersistencePort.createCategory(categoryModel);
    }

    public List<CategoryModel> getCategoriesWithPagination(int page, int size, String sortBy, boolean asc) {

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
    public CategoryModel getCategoryById(Long categoryId) {
        return categoryPersistencePort.getCategoryById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID " + categoryId + " not found."));
    }

}
