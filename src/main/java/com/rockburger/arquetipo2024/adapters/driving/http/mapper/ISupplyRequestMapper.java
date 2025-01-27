package com.rockburger.arquetipo2024.adapters.driving.http.mapper;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddSupplyRequest;
import com.rockburger.arquetipo2024.domain.model.SupplyModel;
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