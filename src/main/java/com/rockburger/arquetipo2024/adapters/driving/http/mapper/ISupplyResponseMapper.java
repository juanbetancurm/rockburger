package com.rockburger.arquetipo2024.adapters.driving.http.mapper;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.SupplyResponse;
import com.rockburger.arquetipo2024.domain.model.SupplyModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ISupplyResponseMapper {
    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleName", source = "article.name")
    @Mapping(target = "supplierEmail", source = "supplier.email")
    SupplyResponse toResponse(SupplyModel supplyModel);

}