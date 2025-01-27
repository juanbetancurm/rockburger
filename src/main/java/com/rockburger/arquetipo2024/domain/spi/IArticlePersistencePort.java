package com.rockburger.arquetipo2024.domain.spi;


import com.rockburger.arquetipo2024.domain.model.ArticleModel;

import java.util.List;
import java.util.Optional;

public interface IArticlePersistencePort {
    ArticleModel save(ArticleModel articleModel);
    List<ArticleModel> listArticles(String sortBy, String sortOrder, int page, int size);
    Optional<ArticleModel> findById(Long id);

}

