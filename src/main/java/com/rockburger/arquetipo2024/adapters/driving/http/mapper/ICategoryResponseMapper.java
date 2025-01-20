package com.rockburger.arquetipo2024.adapters.driving.http.mapper;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.CategoryResponse;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import org.mapstruct.Mapper;


import java.util.List;
@Mapper(componentModel = "spring")
public interface ICategoryResponseMapper {
    CategoryResponse toResponse(CategoryModel categoryModel);
    List<CategoryResponse> toCategoryResponseList(List<CategoryModel> categoryModels);
}
