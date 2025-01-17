package com.rockburger.arquetipo2024.adaptertest;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.adapter.CategoryAdapter;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.CategoryEntity;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper.ICategoryEntityMapper;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.ICategoryRepository;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CategoryAdapterTest {
    @Mock
    private ICategoryRepository categoryRepository;

    @Mock
    private ICategoryEntityMapper categoryEntityMapper;

    @InjectMocks
    private CategoryAdapter categoryAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCategoryByName_shouldReturnCategoryModel_whenCategoryExists() {
        // Given
        String categoryName = "Electronics";
        CategoryEntity categoryEntity = new CategoryEntity(1L, categoryName, "Gadgets and devices", Collections.emptySet());
        CategoryModel categoryModel = new CategoryModel(1L, categoryName, "Gadgets and devices");

        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.of(categoryEntity));
        when(categoryEntityMapper.toModel(categoryEntity)).thenReturn(categoryModel);

        // When
        Optional<CategoryModel> foundCategory = categoryAdapter.getCategoryByName(categoryName);

        // Then
        assertTrue(foundCategory.isPresent());
        assertEquals(categoryModel, foundCategory.get());
        verify(categoryRepository, times(1)).findByName(categoryName);
        verify(categoryEntityMapper, times(1)).toModel(categoryEntity);
    }

    @Test
    void saveCategory_shouldSaveAndReturnCategoryModel() {
        // Given
        CategoryModel categoryModel = new CategoryModel(null, "Books", "Books and literature");
        CategoryEntity categoryEntity = new CategoryEntity(null, "Books", "Books and literature", Collections.emptySet());

        when(categoryEntityMapper.toEntity(categoryModel)).thenReturn(categoryEntity);
        when(categoryRepository.save(categoryEntity)).thenReturn(new CategoryEntity(1L, "Books", "Books and literature", Collections.emptySet()));
        when(categoryEntityMapper.toModel(any(CategoryEntity.class))).thenReturn(new CategoryModel(1L, "Books", "Books and literature"));

        // When
        CategoryModel savedCategory = categoryAdapter.createCategory(categoryModel);

        // Then
        assertNotNull(savedCategory.getId());
        assertEquals("Books", savedCategory.getName());
        verify(categoryRepository, times(1)).save(categoryEntity);
        verify(categoryEntityMapper, times(1)).toModel(any(CategoryEntity.class));
    }
}
