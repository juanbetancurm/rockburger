package com.rockburger.arquetipo2024.usecasetest;

import com.rockburger.arquetipo2024.domain.api.usecase.BrandUseCase;
import com.rockburger.arquetipo2024.domain.exception.NameAlreadyExistsExceptionD;
import com.rockburger.arquetipo2024.domain.model.BrandModel;
import com.rockburger.arquetipo2024.domain.spi.IBrandPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class BrandUseCaseTest {
    @Mock
    private IBrandPersistencePort brandPersistencePort;

    @InjectMocks
    private BrandUseCase brandUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBrand_shouldThrowException_whenBrandNameExists() {
        // Given
        String existingBrandName = "Adidas";
        BrandModel existingBrand = new BrandModel(1L, existingBrandName, "Shoes");

        when(brandPersistencePort.getBrandByName(existingBrandName)).thenReturn(Optional.of(existingBrand));

        BrandModel newBrand = new BrandModel(null, existingBrandName, "New Brand Description");

        // When & Then
        assertThrows(NameAlreadyExistsExceptionD.class, () -> brandUseCase.createBrand(newBrand));
        verify(brandPersistencePort, never()).createBrand(newBrand);
    }

    @Test
    void createBrand_shouldSaveBrand_whenBrandNameDoesNotExist() {
        // Given
        String newBrandName = "Rolex";
        BrandModel newBrand = new BrandModel(null, newBrandName, "Watches");

        when(brandPersistencePort.getBrandByName(newBrandName)).thenReturn(Optional.empty());
        when(brandPersistencePort.createBrand(newBrand)).thenReturn(new BrandModel(1L, newBrandName, "Watches"));

        // When
        BrandModel savedBrand = brandUseCase.createBrand(newBrand);

        // Then
        assertNotNull(savedBrand.getId());
        assertEquals(newBrandName, savedBrand.getName());
        verify(brandPersistencePort, times(1)).createBrand(newBrand);
    }
}
