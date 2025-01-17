package com.rockburger.arquetipo2024.domain.api.usecase;

import com.rockburger.arquetipo2024.domain.api.IBrandServicePort;
import com.rockburger.arquetipo2024.domain.exception.BlankFieldException;
import com.rockburger.arquetipo2024.domain.exception.InvalidParameterException;
import com.rockburger.arquetipo2024.domain.exception.NameAlreadyExistsExceptionD;
import com.rockburger.arquetipo2024.domain.model.BrandModel;
import com.rockburger.arquetipo2024.domain.spi.IBrandPersistencePort;

import java.util.List;
import java.util.Optional;

public class BrandUseCase implements IBrandServicePort {
    private final IBrandPersistencePort brandPersistencePort;

    public BrandUseCase(IBrandPersistencePort brandPersistencePort){
        this.brandPersistencePort = brandPersistencePort;
    }
    @Override
    public BrandModel createBrand (BrandModel brandModel){
        Optional<BrandModel> existingBrand = brandPersistencePort.getBrandByName(brandModel.getName());
        if (brandModel.getName() == null || brandModel.getName().trim().isEmpty() ||
                brandModel.getDescription() == null || brandModel.getDescription().trim().isEmpty()) {
            throw new BlankFieldException("Field cannot be blank");
        }
        if (existingBrand.isPresent()) {
            throw new NameAlreadyExistsExceptionD(brandModel.getName());
        }

        return brandPersistencePort.createBrand(brandModel);
    }

    public List<BrandModel> getBrandsWithPagination(int page, int size, String sortBy, boolean asc) {

        if (page < 0) {
            throw new InvalidParameterException("Page number cannot be negative.");
        }
        if (size <= 0) {
            throw new InvalidParameterException("Page size must be greater than zero.");
        }
        if (sortBy == null || sortBy.trim().isEmpty()) {
            throw new InvalidParameterException("SortBy field must not be null or empty.");
        }
        return brandPersistencePort.getBrandsWithPagination(page, size, sortBy, asc);
    }


    public BrandModel getBrandById(Long brandId) {
        return brandPersistencePort.getBrandById(brandId);
    }
}
