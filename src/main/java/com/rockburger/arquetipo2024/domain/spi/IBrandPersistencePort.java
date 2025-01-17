package com.rockburger.arquetipo2024.domain.spi;

import com.rockburger.arquetipo2024.domain.model.BrandModel;

import java.util.List;
import java.util.Optional;

public interface IBrandPersistencePort {
    BrandModel createBrand (BrandModel brandModel);
    Optional<BrandModel> getBrandByName(String name);
    List<BrandModel> getBrandsWithPagination(int page, int size, String sortby, boolean asc);
    BrandModel getBrandById(Long brandId);

}
