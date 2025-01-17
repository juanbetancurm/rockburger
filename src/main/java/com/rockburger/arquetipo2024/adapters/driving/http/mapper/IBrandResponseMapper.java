package com.rockburger.arquetipo2024.adapters.driving.http.mapper;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.BrandResponse;
import com.rockburger.arquetipo2024.domain.model.BrandModel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IBrandResponseMapper {


    BrandResponse toResponse(BrandModel brandModel);
    List<BrandResponse> toBrandResponseList(List<BrandModel> brandModels);
}
