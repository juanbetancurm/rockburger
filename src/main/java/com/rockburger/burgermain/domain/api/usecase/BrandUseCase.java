package com.rockburger.burgermain.domain.api.usecase;

import com.rockburger.burgermain.domain.api.IBrandServicePort;
import com.rockburger.burgermain.domain.exception.BlankFieldException;
import com.rockburger.burgermain.domain.exception.NotFoundException;
import com.rockburger.burgermain.domain.exception.InvalidParameterException;
import com.rockburger.burgermain.domain.exception.NameAlreadyExistsExceptionD;
import com.rockburger.burgermain.domain.model.BrandModel;
import com.rockburger.burgermain.domain.spi.IBrandPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Use case implementation for Brand domain operations.
 * Implements business logic and validation rules for brand management.
 */
public class BrandUseCase implements IBrandServicePort {
    private static final Logger logger = LoggerFactory.getLogger(BrandUseCase.class);

    private final IBrandPersistencePort brandPersistencePort;

    public BrandUseCase(IBrandPersistencePort brandPersistencePort) {
        this.brandPersistencePort = brandPersistencePort;
    }

    @Override
    public BrandModel createBrand(BrandModel brandModel) {
        logger.info("Creating brand: {}", brandModel.getName());

        // Validate required fields
        validateBrandFields(brandModel);

        // Check if brand name already exists
        Optional<BrandModel> existingBrand = brandPersistencePort.getBrandByName(brandModel.getName());
        if (existingBrand.isPresent()) {
            logger.warn("Brand name already exists: {}", brandModel.getName());
            throw new NameAlreadyExistsExceptionD(brandModel.getName());
        }

        BrandModel createdBrand = brandPersistencePort.createBrand(brandModel);
        logger.info("Brand created successfully with ID: {}", createdBrand.getId());
        return createdBrand;
    }

    @Override
    public List<BrandModel> getBrandsWithPagination(int page, int size, String sortBy, boolean asc) {
        logger.debug("Getting brands with pagination: page={}, size={}, sortBy={}, asc={}", page, size, sortBy, asc);

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

        return brandPersistencePort.getBrandsWithPagination(page, size, sortBy, asc);
    }

    @Override
    public BrandModel getBrandById(Long brandId) {
        logger.debug("Getting brand by ID: {}", brandId);

        if (brandId == null) {
            throw new InvalidParameterException("Brand ID cannot be null");
        }

        BrandModel brand = brandPersistencePort.getBrandById(brandId);
        if (brand == null) {
            throw new NotFoundException("Brand with ID " + brandId + " not found.");
        }

        return brand;
    }

    @Override
    public BrandModel updateBrand(BrandModel brandModel) {
        logger.info("Updating brand ID: {}", brandModel.getId());

        // Validate ID is present
        if (brandModel.getId() == null) {
            throw new InvalidParameterException("Brand ID is required for update");
        }

        // Validate required fields
        validateBrandFields(brandModel);

        // Check if brand exists
        BrandModel existingBrand = brandPersistencePort.getBrandById(brandModel.getId());
        if (existingBrand == null) {
            throw new NotFoundException("Brand with ID " + brandModel.getId() + " not found.");
        }

        // Check if the new name already exists (excluding current brand)
        Optional<BrandModel> brandWithSameName = brandPersistencePort.getBrandByName(brandModel.getName());
        if (brandWithSameName.isPresent() && !brandWithSameName.get().getId().equals(brandModel.getId())) {
            logger.warn("Brand name already exists: {}", brandModel.getName());
            throw new NameAlreadyExistsExceptionD(brandModel.getName());
        }

        BrandModel updatedBrand = brandPersistencePort.updateBrand(brandModel);
        logger.info("Brand updated successfully: {}", updatedBrand.getId());
        return updatedBrand;
    }

    @Override
    public void deleteBrand(Long brandId) {
        logger.info("Deleting brand ID: {}", brandId);

        if (brandId == null) {
            throw new InvalidParameterException("Brand ID cannot be null");
        }

        // Check if brand exists
        BrandModel existingBrand = brandPersistencePort.getBrandById(brandId);
        if (existingBrand == null) {
            throw new NotFoundException("Brand with ID " + brandId + " not found.");
        }

        // TODO: Add validation to check if brand is in use by articles
        // This would prevent deletion of brands that have associated articles

        brandPersistencePort.deleteBrand(brandId);
        logger.info("Brand deleted successfully: {}", brandId);
    }

    @Override
    public boolean existsById(Long brandId) {
        if (brandId == null) {
            return false;
        }
        BrandModel brand = brandPersistencePort.getBrandById(brandId);
        return brand != null;
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return brandPersistencePort.getBrandByName(name).isPresent();
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        Optional<BrandModel> brand = brandPersistencePort.getBrandByName(name);
        return brand.isPresent() && !brand.get().getId().equals(excludeId);
    }

    @Override
    public List<BrandModel> getAllBrands() {
        logger.debug("Getting all brands");
        // Use a large page size to get all brands
        return brandPersistencePort.getBrandsWithPagination(0, 1000, "name", true);
    }

    /**
     * Validates that required brand fields are present and valid.
     *
     * @param brandModel the brand to validate
     * @throws BlankFieldException if required fields are missing or empty
     */
    private void validateBrandFields(BrandModel brandModel) {
        if (brandModel == null) {
            throw new BlankFieldException("Brand cannot be null");
        }

        if (brandModel.getName() == null || brandModel.getName().trim().isEmpty()) {
            throw new BlankFieldException("Brand name cannot be blank");
        }

        if (brandModel.getDescription() == null || brandModel.getDescription().trim().isEmpty()) {
            throw new BlankFieldException("Brand description cannot be blank");
        }

        // Validate length constraints
        if (brandModel.getName().length() > 50) {
            throw new InvalidParameterException("Brand name cannot exceed 50 characters");
        }

        if (brandModel.getDescription().length() > 90) {
            throw new InvalidParameterException("Brand description cannot exceed 90 characters");
        }
    }
}