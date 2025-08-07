package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driving.http.dto.request.AddArticleRequest;
import com.rockburger.burgermain.domain.model.ArticleModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IArticleRequestMapper {
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "brandId", source = "brandId", ignore = true)
    ArticleModel toModel(AddArticleRequest addArticleRequest);
}


