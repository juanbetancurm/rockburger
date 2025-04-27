package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driving.http.dto.response.SupplyResponse;
import com.rockburger.burgermain.domain.model.SupplyModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ISupplyResponseMapper {
    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleName", source = "article.name")
    @Mapping(target = "supplierEmail", source = "supplier.email")
    SupplyResponse toResponse(SupplyModel supplyModel);

}