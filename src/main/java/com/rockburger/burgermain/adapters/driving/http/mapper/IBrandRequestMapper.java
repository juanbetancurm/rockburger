package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driving.http.dto.request.AddBrandRequest;
import com.rockburger.burgermain.domain.model.BrandModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IBrandRequestMapper {

    BrandModel addRequestToBrandModel(AddBrandRequest addBrandRequest);
}
