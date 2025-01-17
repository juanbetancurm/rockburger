package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.adapter;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.CategoryEntity;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper.ICategoryEntityMapper;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.ICategoryRepository;

import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import com.rockburger.arquetipo2024.domain.spi.ICategoryPersistencePort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryAdapter implements ICategoryPersistencePort {
    private final ICategoryRepository categoryRepository;
    private final ICategoryEntityMapper categoryEntityMapper;

    public CategoryAdapter(ICategoryRepository categoryRepository, ICategoryEntityMapper categoryEntityMapper){
        this.categoryRepository = categoryRepository;
        this.categoryEntityMapper = categoryEntityMapper;
    }
    @Override
    public CategoryModel createCategory(CategoryModel categoryModel){
        CategoryEntity categoryEntity= categoryEntityMapper.toEntity(categoryModel);
        CategoryEntity savedEntity = categoryRepository.save(categoryEntity);
        return categoryEntityMapper.toModel(savedEntity);
    }
    @Override
    public Optional<CategoryModel> getCategoryByName(String name) {
        return categoryRepository.findByName(name).map(categoryEntityMapper::toModel);
    }
    @Override
    public List<CategoryModel> getCategoriesWithPagination(int page, int size, String sortby, boolean asc){
        Pageable pageable = PageRequest.of(page, size, asc ? Sort.by(sortby).ascending() : Sort.by(sortby).descending());
        return categoryRepository.findAll(pageable)
                .stream()
                .map(categoryEntityMapper::toModel)
                .toList();

    }
    @Override
    public List<CategoryEntity> getAllCategories() {

        return categoryRepository.findAll();
    }

    @Override
    public CategoryModel updateCategory(CategoryModel categoryModel) {
        CategoryEntity categoryEntity = categoryEntityMapper.toEntity(categoryModel);
        categoryEntity = categoryRepository.save(categoryEntity);
        return categoryEntityMapper.toModel(categoryEntity);
    }
    @Override
    public Optional<CategoryModel> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryEntityMapper::toModel);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

}
