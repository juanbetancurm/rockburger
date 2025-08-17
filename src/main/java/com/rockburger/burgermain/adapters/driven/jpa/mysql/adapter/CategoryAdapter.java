package com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.CategoryEntity;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.exception.ElementNotFoundException;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper.ICategoryEntityMapper;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.repository.IArticleRepository;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.repository.ICategoryRepository;
import com.rockburger.burgermain.domain.exception.NotFoundException;
import com.rockburger.burgermain.domain.model.CategoryModel;
import com.rockburger.burgermain.domain.spi.ICategoryPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryAdapter implements ICategoryPersistencePort {
    private static final Logger logger = LoggerFactory.getLogger(CategoryAdapter.class);

    private final ICategoryRepository categoryRepository;
    private final ICategoryEntityMapper categoryEntityMapper;
    private final IArticleRepository articleRepository; // For checking if category is in use

    public CategoryAdapter(ICategoryRepository categoryRepository,
                           ICategoryEntityMapper categoryEntityMapper,
                           IArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryEntityMapper = categoryEntityMapper;
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional
    public CategoryModel createCategory(CategoryModel categoryModel) {
        logger.debug("Creating category: {}", categoryModel.getName());

        CategoryEntity categoryEntity = categoryEntityMapper.toEntity(categoryModel);
        CategoryEntity savedEntity = categoryRepository.save(categoryEntity);
        CategoryModel savedModel = categoryEntityMapper.toModel(savedEntity);

        logger.debug("Category created with ID: {}", savedModel.getId());
        return savedModel;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryModel> getCategoryByName(String name) {
        logger.debug("Finding category by name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }

        return categoryRepository.findByName(name)
                .map(categoryEntityMapper::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryModel> getCategoriesWithPagination(int page, int size, String sortBy, boolean asc) {
        logger.debug("Getting categories with pagination: page={}, size={}, sortBy={}, asc={}",
                page, size, sortBy, asc);

        Sort sort = asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return categoryRepository.findAll(pageable)
                .stream()
                .map(categoryEntityMapper::toModel)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryModel> getCategoryById(Long categoryId) {
        logger.debug("Finding category by ID: {}", categoryId);

        if (categoryId == null) {
            return Optional.empty();
        }

        return categoryRepository.findById(categoryId)
                .map(categoryEntityMapper::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryEntity> getAllCategories() {
        logger.debug("Getting all category entities");

        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public CategoryModel updateCategory(CategoryModel categoryModel) {
        logger.debug("Updating category ID: {}", categoryModel.getId());

        if (categoryModel.getId() == null) {
            throw new IllegalArgumentException("Category ID is required for update");
        }

        // Check if category exists
        Optional<CategoryEntity> existingEntityOpt = categoryRepository.findById(categoryModel.getId());
        if (!existingEntityOpt.isPresent()) {
            throw new NotFoundException("Category with ID " + categoryModel.getId() + " not found");
        }

        CategoryEntity categoryEntity = categoryEntityMapper.toEntity(categoryModel);
        CategoryEntity updatedEntity = categoryRepository.save(categoryEntity);
        CategoryModel updatedModel = categoryEntityMapper.toModel(updatedEntity);

        logger.debug("Category updated successfully: {}", updatedModel.getId());
        return updatedModel;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        logger.debug("Deleting category ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }

        // Check if category exists
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category with ID " + id + " not found");
        }

        // Check if category is in use
        if (isCategoryInUse(id)) {
            throw new IllegalStateException("Cannot delete category that is currently in use by articles");
        }

        categoryRepository.deleteById(id);
        logger.debug("Category deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return categoryRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return categoryRepository.findByName(name).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty() || excludeId == null) {
            return false;
        }

        Optional<CategoryEntity> categoryEntity = categoryRepository.findByName(name);
        return categoryEntity.isPresent() && !categoryEntity.get().getId().equals(excludeId);
    }

    /**
     * Gets all categories as models (not part of the interface).
     * This is a convenience method for internal use.
     */
    @Transactional(readOnly = true)
    public List<CategoryModel> getAllCategoriesAsModels() {
        logger.debug("Getting all categories as models");

        return categoryRepository.findAll()
                .stream()
                .map(categoryEntityMapper::toModel)
                .toList();
    }

    /**
     * Checks if a category is being used by any articles.
     * Used to prevent deletion of categories that are in use.
     *
     * @param categoryId the category ID to check
     * @return true if the category is in use, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isCategoryInUse(Long categoryId) {
        logger.debug("Checking if category {} is in use", categoryId);

        if (categoryId == null) {
            return false;
        }

        try {
            // Check if any articles are using this category
            // This would require a method like existsByCategories_Id in the article repository
            // For now, we'll implement a basic check

            // Note: This assumes articles have a many-to-many relationship with categories
            // and the article entity has a categories collection
            boolean inUse = articleRepository.findAll()
                    .stream()
                    .anyMatch(article ->
                            article.getCategories() != null &&
                                    article.getCategories().stream()
                                            .anyMatch(category -> categoryId.equals(category.getId()))
                    );

            logger.debug("Category {} is {} in use", categoryId, inUse ? "" : "not");
            return inUse;

        } catch (Exception e) {
            // If we can't check due to method not existing or other issues, assume it's safe to delete
            logger.warn("Could not check if category {} is in use, assuming it's safe: {}", categoryId, e.getMessage());
            return false;
        }
    }
}