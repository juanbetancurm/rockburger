package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driving.http.dto.response.CategoryResponse;
import com.rockburger.burgermain.domain.model.CategoryModel;
import org.mapstruct.Mapper;


import java.util.List;
@Mapper(componentModel = "spring")
public interface ICategoryResponseMapper {
    CategoryResponse toResponse(CategoryModel categoryModel);
    List<CategoryResponse> toCategoryResponseList(List<CategoryModel> categoryModels);
}
