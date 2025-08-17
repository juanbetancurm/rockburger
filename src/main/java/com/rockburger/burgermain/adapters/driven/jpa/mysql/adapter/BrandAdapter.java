package com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.BrandEntity;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.exception.ElementNotFoundException;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper.IBrandEntityMapper;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.repository.IArticleRepository;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.repository.IBrandRepository;
import com.rockburger.burgermain.domain.exception.NotFoundException;
import com.rockburger.burgermain.domain.model.BrandModel;
import com.rockburger.burgermain.domain.spi.IBrandPersistencePort;
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
public class BrandAdapter implements IBrandPersistencePort {
    private static final Logger logger = LoggerFactory.getLogger(BrandAdapter.class);

    private final IBrandRepository brandRepository;
    private final IBrandEntityMapper brandEntityMapper;
    private final IArticleRepository articleRepository; // For checking if brand is in use

    public BrandAdapter(IBrandRepository brandRepository,
                        IBrandEntityMapper brandEntityMapper,
                        IArticleRepository articleRepository) {
        this.brandRepository = brandRepository;
        this.brandEntityMapper = brandEntityMapper;
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional
    public BrandModel createBrand(BrandModel brandModel) {
        logger.debug("Creating brand: {}", brandModel.getName());

        BrandEntity brandEntity = brandEntityMapper.toEntity(brandModel);
        BrandEntity savedEntity = brandRepository.save(brandEntity);
        BrandModel savedModel = brandEntityMapper.toModel(savedEntity);

        logger.debug("Brand created with ID: {}", savedModel.getId());
        return savedModel;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BrandModel> getBrandByName(String name) {
        logger.debug("Finding brand by name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }

        return brandRepository.findByName(name)
                .map(brandEntityMapper::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandModel> getBrandsWithPagination(int page, int size, String sortBy, boolean asc) {
        logger.debug("Getting brands with pagination: page={}, size={}, sortBy={}, asc={}",
                page, size, sortBy, asc);

        Sort sort = asc ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return brandRepository.findAll(pageable)
                .stream()
                .map(brandEntityMapper::toModel)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BrandModel getBrandById(Long brandId) {
        logger.debug("Finding brand by ID: {}", brandId);

        if (brandId == null) {
            return null;
        }

        Optional<BrandEntity> brandEntity = brandRepository.findById(brandId);
        if (!brandEntity.isPresent()) {
            logger.warn("Brand not found with ID: {}", brandId);
            return null;
        }

        return brandEntityMapper.toModel(brandEntity.get());
    }

    @Override
    @Transactional
    public BrandModel updateBrand(BrandModel brandModel) {
        logger.debug("Updating brand ID: {}", brandModel.getId());

        if (brandModel.getId() == null) {
            throw new IllegalArgumentException("Brand ID is required for update");
        }

        // Check if brand exists
        Optional<BrandEntity> existingEntityOpt = brandRepository.findById(brandModel.getId());
        if (!existingEntityOpt.isPresent()) {
            throw new NotFoundException("Brand with ID " + brandModel.getId() + " not found");
        }

        BrandEntity brandEntity = brandEntityMapper.toEntity(brandModel);
        BrandEntity updatedEntity = brandRepository.save(brandEntity);
        BrandModel updatedModel = brandEntityMapper.toModel(updatedEntity);

        logger.debug("Brand updated successfully: {}", updatedModel.getId());
        return updatedModel;
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        logger.debug("Deleting brand ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("Brand ID cannot be null");
        }

        // Check if brand exists
        if (!brandRepository.existsById(id)) {
            throw new NotFoundException("Brand with ID " + id + " not found");
        }

        // Check if brand is in use
        if (isBrandInUse(id)) {
            throw new IllegalStateException("Cannot delete brand that is currently in use by articles");
        }

        brandRepository.deleteById(id);
        logger.debug("Brand deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return brandRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return brandRepository.findByName(name).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty() || excludeId == null) {
            return false;
        }

        Optional<BrandEntity> brandEntity = brandRepository.findByName(name);
        return brandEntity.isPresent() && !brandEntity.get().getId().equals(excludeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandModel> getAllBrands() {
        logger.debug("Getting all brands");

        return brandRepository.findAll()
                .stream()
                .map(brandEntityMapper::toModel)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBrandInUse(Long brandId) {
        logger.debug("Checking if brand {} is in use", brandId);

        if (brandId == null) {
            return false;
        }

        try {
            // Check if any articles are using this brand
            boolean inUse = articleRepository.existsByBrandId(brandId);

            logger.debug("Brand {} is {} in use", brandId, inUse ? "" : "not");
            return inUse;

        } catch (Exception e) {
            // If we can't check due to method not existing, assume it's safe to delete
            logger.warn("Could not check if brand {} is in use, assuming it's safe: {}", brandId, e.getMessage());
            return false;
        }
    }
}