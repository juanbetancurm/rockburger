package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.BrandModel;

import java.util.List;

public interface IBrandServicePort {
    BrandModel createBrand(BrandModel brandModel);

    List<BrandModel> getBrandsWithPagination(int page, int size, String sortBy, boolean asc);
    BrandModel getBrandById(Long brandId);
}
