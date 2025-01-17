package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.CategoryEntity;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ICategoryEntityMapper {
    CategoryEntity toEntity(CategoryModel categoryModel);
    CategoryModel toModel(CategoryEntity categoryEntity);
}
