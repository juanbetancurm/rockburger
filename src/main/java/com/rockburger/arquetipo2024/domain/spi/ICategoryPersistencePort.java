package com.rockburger.arquetipo2024.domain.spi;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.CategoryEntity;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;

import java.util.List;
import java.util.Optional;

public interface ICategoryPersistencePort {
    CategoryModel createCategory (CategoryModel categoryModel);
    Optional<CategoryModel> getCategoryByName(String name);

    List<CategoryModel> getCategoriesWithPagination(int page, int size, String sortby, boolean asc);

    Optional<CategoryModel> getCategoryById(Long categoryId);

    List<CategoryEntity> getAllCategories();
    CategoryModel updateCategory(CategoryModel categoryModel);
    void deleteCategory (Long id);
}
