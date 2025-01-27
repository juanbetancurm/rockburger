package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.SupplyEntity;
import com.rockburger.arquetipo2024.domain.model.SupplyModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ISupplyEntityMapper {
    @Mapping(target = "article", source = "article")
    @Mapping(target = "supplier", source = "supplier")
    SupplyEntity toEntity(SupplyModel supplyModel);

    @Mapping(target = "article", source = "article")
    @Mapping(target = "supplier", source = "supplier")
    SupplyModel toModel(SupplyEntity supplyEntity);
}