package com.rockburger.arquetipo2024.adapters.driving.http.mapper;


import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddCategoryRequest;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ICategoryRequestMapper {

    CategoryModel addRequestToCategoryModel(AddCategoryRequest addCategoryRequest);
}
