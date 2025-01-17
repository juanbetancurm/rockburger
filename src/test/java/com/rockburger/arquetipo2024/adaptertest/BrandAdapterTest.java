package com.rockburger.arquetipo2024.adapterstest.driven.jpa.mysqltest.adaptertest;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.adapter.BrandAdapter;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.BrandEntity;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper.IBrandEntityMapper;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.IBrandRepository;
import com.rockburger.arquetipo2024.domain.model.BrandModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class BrandAdapterTest {

    @Mock
    private IBrandRepository brandRepository;

    @Mock
    private IBrandEntityMapper brandEntityMapper;

    @InjectMocks
    private BrandAdapter brandAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getBrandByName_shouldReturnBrandModel_whenBrandExists() {
        // Given
        String brandName = "Adidas";
        BrandEntity brandEntity = new BrandEntity(1L, brandName, "Shoes");
        BrandModel brandModel = new BrandModel(1L, brandName, "Shoes");

        when(brandRepository.findByName(brandName)).thenReturn(Optional.of(brandEntity));
        when(brandEntityMapper.toModel(brandEntity)).thenReturn(brandModel);

        // When
        Optional<BrandModel> foundBrand = brandAdapter.getBrandByName(brandName);

        // Then
        assertTrue(foundBrand.isPresent());
        assertEquals(brandModel, foundBrand.get());
        verify(brandRepository, times(1)).findByName(brandName);
        verify(brandEntityMapper, times(1)).toModel(brandEntity);
    }

    @Test
    void saveBrand_shouldSaveAndReturnBrandModel() {
        // Given
        BrandModel brandModel = new BrandModel(null, "Rolex", "Watches");
        BrandEntity brandEntity = new BrandEntity(null, "Rolex", "Watches");

        when(brandEntityMapper.toEntity(brandModel)).thenReturn(brandEntity);
        when(brandRepository.save(brandEntity)).thenReturn(new BrandEntity(1L, "Rolex", "Watches"));
        when(brandEntityMapper.toModel(any(BrandEntity.class))).thenReturn(new BrandModel(1L, "Rolex", "Watches"));

        // When
        BrandModel savedBrand = brandAdapter.createBrand(brandModel);

        // Then
        assertNotNull(savedBrand.getId());
        assertEquals("Rolex", savedBrand.getName());
        verify(brandRepository, times(1)).save(brandEntity);
        verify(brandEntityMapper, times(1)).toModel(any(BrandEntity.class));
    }
}
