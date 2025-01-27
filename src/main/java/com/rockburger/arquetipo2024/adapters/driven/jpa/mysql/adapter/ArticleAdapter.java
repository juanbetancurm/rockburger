package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.adapter;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.ArticleEntity;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper.IArticleEntityMapper;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.IArticleRepository;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.IBrandRepository;
import com.rockburger.arquetipo2024.domain.model.ArticleModel;
import com.rockburger.arquetipo2024.domain.spi.IArticlePersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ArticleAdapter implements IArticlePersistencePort{
    private static final Logger logger = LoggerFactory.getLogger(ArticleAdapter.class);
    private final IArticleRepository articleRepository;
    private final IArticleEntityMapper articleEntityMapper;
    private final IBrandRepository brandRepository;

    public ArticleAdapter(IArticleRepository articleRepository, IArticleEntityMapper articleEntityMapper, IBrandRepository brandRepository) {
        this.articleRepository = articleRepository;
        this.articleEntityMapper = articleEntityMapper;
        this.brandRepository = brandRepository;
    }

    @Override
    public ArticleModel save(ArticleModel articleModel) {
        // Map ArticleModel to ArticleEntity
        logger.info("Saving ArticleModel in persistence layer: {}", articleModel);
        ArticleEntity articleEntity = articleEntityMapper.toEntity(articleModel);

        // Save entity in the database
        logger.info("Mapped ArticleEntity: {}", articleEntity);
        ArticleEntity savedArticle = articleRepository.save(articleEntity);

        ArticleModel savedModel = articleEntityMapper.toModel(savedArticle);

        logger.info("Saved ArticleEntity and returning ArticleModel: {}", savedModel);

        return savedModel;
    }

    @Override
    public List<ArticleModel> listArticles(String sortBy, String sortOrder, int page, int size) {
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return articleRepository.findAll(pageable)
                .stream()
                .map(articleEntityMapper::toModel)
                .toList();
    }


    @Override
    public Optional<ArticleModel> findById(Long id) {
        logger.debug("Finding article by ID: {}", id);
        return articleRepository.findById(id)
                .map(articleEntityMapper::toModel);
    }


}
