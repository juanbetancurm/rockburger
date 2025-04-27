package com.rockburger.burgermain.adapters.driving.http.mapper;


import com.rockburger.burgermain.adapters.driving.http.dto.request.AddCategoryRequest;
import com.rockburger.burgermain.domain.model.CategoryModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ICategoryRequestMapper {

    CategoryModel addRequestToCategoryModel(AddCategoryRequest addCategoryRequest);
}
