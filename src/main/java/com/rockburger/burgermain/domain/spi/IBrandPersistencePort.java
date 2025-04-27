package com.rockburger.burgermain.domain.spi;

import com.rockburger.burgermain.domain.model.BrandModel;

import java.util.List;
import java.util.Optional;

public interface IBrandPersistencePort {
    BrandModel createBrand (BrandModel brandModel);
    Optional<BrandModel> getBrandByName(String name);
    List<BrandModel> getBrandsWithPagination(int page, int size, String sortby, boolean asc);
    BrandModel getBrandById(Long brandId);

}
