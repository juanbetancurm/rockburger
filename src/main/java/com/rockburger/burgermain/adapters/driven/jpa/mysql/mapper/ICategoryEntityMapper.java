package com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.CategoryEntity;
import com.rockburger.burgermain.domain.model.CategoryModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ICategoryEntityMapper {
    CategoryEntity toEntity(CategoryModel categoryModel);
    CategoryModel toModel(CategoryEntity categoryEntity);
}
