package com.rockburger.arquetipo2024.adapters.driving.http.mapper;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddBrandRequest;
import com.rockburger.arquetipo2024.domain.model.BrandModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IBrandRequestMapper {

    BrandModel addRequestToBrandModel(AddBrandRequest addBrandRequest);
}
