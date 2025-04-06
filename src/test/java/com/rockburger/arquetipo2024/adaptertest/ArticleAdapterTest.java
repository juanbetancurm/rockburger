package com.rockburger.arquetipo2024.adaptertest;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.adapter.ArticleAdapter;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.ArticleEntity;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper.IArticleEntityMapper;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.IArticleRepository;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.IBrandRepository;
import com.rockburger.arquetipo2024.domain.model.ArticleModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ArticleAdapterTest {

    @InjectMocks
    private ArticleAdapter articleAdapter;

    @Mock
    private IArticleRepository articleRepository;

    @Mock
    private IArticleEntityMapper articleEntityMapper;

    @Mock
    private IBrandRepository brandRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_ShouldSaveArticle_WhenValidDataIsProvided() {
        // Arrange
        ArticleModel articleModel = new ArticleModel();
        ArticleEntity articleEntity = new ArticleEntity();
        ArticleEntity savedEntity = new ArticleEntity();
        ArticleModel expectedModel = new ArticleModel();

        when(articleEntityMapper.toEntity(articleModel)).thenReturn(articleEntity);
        when(articleRepository.save(articleEntity)).thenReturn(savedEntity);
        when(articleEntityMapper.toModel(savedEntity)).thenReturn(expectedModel);

        // Act
        ArticleModel result = articleAdapter.save(articleModel);

        // Assert
        assertEquals(expectedModel, result);
        verify(articleRepository).save(articleEntity);
    }

    @Test
    void listArticles_ShouldReturnArticles_WhenValidParametersProvided() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        ArticleEntity articleEntity = new ArticleEntity();
        ArticleModel articleModel = new ArticleModel();
        List<ArticleEntity> articleEntities = List.of(articleEntity);
        List<ArticleModel> expectedArticles = List.of(articleModel);

        when(articleRepository.findAll(pageable)).thenReturn(new PageImpl<>(articleEntities));
        when(articleEntityMapper.toModel(articleEntity)).thenReturn(articleModel);

        // Act
        List<ArticleModel> result = articleAdapter.listArticles("name", "asc", 0, 10);

        // Assert
        assertEquals(expectedArticles, result);
        verify(articleRepository).findAll(pageable);
    }
}
