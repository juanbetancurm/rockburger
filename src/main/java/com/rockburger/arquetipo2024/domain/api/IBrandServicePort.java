package com.rockburger.arquetipo2024.domain.api;

import com.rockburger.arquetipo2024.domain.model.BrandModel;

import java.util.List;

public interface IBrandServicePort {
    BrandModel createBrand(BrandModel brandModel);

    List<BrandModel> getBrandsWithPagination(int page, int size, String sortBy, boolean asc);
    BrandModel getBrandById(Long brandId);
}
