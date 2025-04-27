package com.rockburger.burgermain.domain.spi;


import com.rockburger.burgermain.domain.model.ArticleModel;

import java.util.List;
import java.util.Optional;

public interface IArticlePersistencePort {
    ArticleModel save(ArticleModel articleModel);
    List<ArticleModel> listArticles(String sortBy, String sortOrder, int page, int size);
    Optional<ArticleModel> findById(Long id);

}

