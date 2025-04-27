package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driving.http.dto.request.AddSupplyRequest;
import com.rockburger.burgermain.domain.model.SupplyModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ISupplyRequestMapper {
    @Mapping(target = "article", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplyDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    SupplyModel toModel(AddSupplyRequest request);
}