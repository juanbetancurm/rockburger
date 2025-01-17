package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.ArticleEntity;
import com.rockburger.arquetipo2024.domain.model.ArticleModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IArticleEntityMapper {
    @Mapping(target = "brand", source = "brand")
    ArticleEntity toEntity(ArticleModel articleModel);
    @Mapping(target = "brand", source = "brand")
    ArticleModel toModel(ArticleEntity articleEntity);
}
