package com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.BrandEntity;
import com.rockburger.burgermain.domain.model.BrandModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IBrandEntityMapper {
    BrandEntity toEntity(BrandModel brandModel);
    BrandModel toModel(BrandEntity brandEntity);
}
