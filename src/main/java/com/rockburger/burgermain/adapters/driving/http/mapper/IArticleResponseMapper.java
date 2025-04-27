package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driving.http.dto.response.ArticleResponse;
import com.rockburger.burgermain.domain.model.ArticleModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IArticleResponseMapper {
    @Mapping(target = "brandId", source = "brandId")
    ArticleResponse toResponse(ArticleModel articleModel);
    List<ArticleResponse> toArticleResponseList(List<ArticleModel> articleModels);
}
