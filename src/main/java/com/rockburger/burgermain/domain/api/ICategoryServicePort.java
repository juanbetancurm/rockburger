package com.rockburger.burgermain.domain.api;


import com.rockburger.burgermain.domain.model.CategoryModel;

import java.util.List;

public interface ICategoryServicePort {

    CategoryModel getCategoryById(Long categoryId);
    CategoryModel createCategory(CategoryModel categoryModel);
    List<CategoryModel> getCategoriesWithPagination(int page, int size, String sortBy, boolean asc);

}
