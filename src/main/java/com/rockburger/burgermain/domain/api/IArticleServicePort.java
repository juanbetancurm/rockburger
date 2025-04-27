package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.ArticleModel;

import java.util.List;

public interface IArticleServicePort {
    ArticleModel createNewArticle(ArticleModel articleModel);
    List<ArticleModel> listArticles(String sortBy, String sortOrder, int page, int size);
    ArticleModel getArticleById(Long articleId);

}
