package com.rockburger.arquetipo2024.usecasetest;

import com.rockburger.arquetipo2024.domain.api.usecase.CategoryUseCase;
import com.rockburger.arquetipo2024.domain.exception.NameAlreadyExistsExceptionD;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import com.rockburger.arquetipo2024.domain.spi.ICategoryPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryUseCaseTest {
    @Mock
    private ICategoryPersistencePort categoryPersistencePort;

    @InjectMocks
    private CategoryUseCase categoryUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCategory_shouldThrowException_whenCategoryNameExists() {
        // Given
        String existingCategoryName = "Electronics";
        CategoryModel existingCategory = new CategoryModel(1L, existingCategoryName, "Gadgets and devices");

        when(categoryPersistencePort.getCategoryByName(existingCategoryName)).thenReturn(Optional.of(existingCategory));

        CategoryModel newCategory = new CategoryModel(null, existingCategoryName, "New Category Description");

        // When & Then
        assertThrows(NameAlreadyExistsExceptionD.class, () -> categoryUseCase.createCategory(newCategory));
        verify(categoryPersistencePort, never()).createCategory(newCategory);
    }

    @Test
    void createCategory_shouldSaveCategory_whenCategoryNameDoesNotExist() {
        // Given
        String newCategoryName = "Books";
        CategoryModel newCategory = new CategoryModel(null, newCategoryName, "Books and literature");

        when(categoryPersistencePort.getCategoryByName(newCategoryName)).thenReturn(Optional.empty());
        when(categoryPersistencePort.createCategory(newCategory)).thenReturn(new CategoryModel(1L, newCategoryName, "Books and literature"));

        // When
        CategoryModel savedCategory = categoryUseCase.createCategory(newCategory);

        // Then
        assertNotNull(savedCategory.getId());
        assertEquals(newCategoryName, savedCategory.getName());
        verify(categoryPersistencePort, times(1)).createCategory(newCategory);
    }
}
