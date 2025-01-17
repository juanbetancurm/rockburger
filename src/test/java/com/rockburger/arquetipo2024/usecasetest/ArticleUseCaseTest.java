package com.rockburger.arquetipo2024.usecasetest;
import com.rockburger.arquetipo2024.adapters.driving.http.controller.ArticleRestController;
import com.rockburger.arquetipo2024.domain.api.ICategoryServicePort;
import com.rockburger.arquetipo2024.domain.api.usecase.ArticleUseCase;
import com.rockburger.arquetipo2024.domain.exception.InvalidCategoryCountException;
import com.rockburger.arquetipo2024.domain.model.ArticleModel;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import com.rockburger.arquetipo2024.domain.spi.IArticlePersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ArticleUseCaseTest {

    private static final Logger logger = LoggerFactory.getLogger(ArticleUseCaseTest.class);

    @Mock
    private IArticlePersistencePort articlePersistencePort;

    @Mock
    private ICategoryServicePort categoryServicePort;

    @InjectMocks
    private ArticleUseCase articleUseCase;
    @InjectMocks
    private ArticleRestController articleRestController;

    private ArticleModel articleModel;
    private Set<Long> categoryIds;
    private Set<CategoryModel> categoryModels;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Prepare the category IDs and models
        categoryIds = new HashSet<>();
        categoryIds.add(1L);
        categoryIds.add(2L);

        categoryModels = new HashSet<>();
        categoryModels.add(new CategoryModel(1L, "Category 1", "Description 1"));
        categoryModels.add(new CategoryModel(2L, "Category 2", "Description 2"));

        //articleModel = new ArticleModel(null, "Article Name", "Article Description", 5, 100.50, new HashSet<>());

        // Mock the categoryServicePort to return the expected category models
        when(categoryServicePort.getCategoryById(1L)).thenReturn(new CategoryModel(1L, "Category 1", "Description 1"));
        when(categoryServicePort.getCategoryById(2L)).thenReturn(new CategoryModel(2L, "Category 2", "Description 2"));
    }

    @Test
    void createNewArticle_Success() {
        // Log expected and actual values at each stage
        logger.info("Test: createNewArticle_Success");

        // Add category IDs to the article model
        articleModel.setCategories(categoryModels);

        // Mock the persistence port to return the saved article
        when(articlePersistencePort.save(any(ArticleModel.class))).thenReturn(articleModel);

        // Expected values
        logger.info("Expected category IDs: {}", categoryIds);
        logger.info("Expected category models: {}", categoryModels);

        // Call the createNewArticle method
        ArticleModel result = articleUseCase.createNewArticle(articleModel);

        // Actual values
        logger.info("Actual category IDs: {}", result.getCategoryIds());
        logger.info("Actual category models: {}", result.getCategories());

        // Verify the expected behavior
        assertNotNull(result);
        assertEquals(articleModel.getName(), result.getName());
        assertEquals(articleModel.getDescription(), result.getDescription());
        assertEquals(articleModel.getCategories().size(), result.getCategories().size());

        logger.info("Test passed: createNewArticle_Success");
    }

    @Test
    void createNewArticle_Fail_InvalidCategoryCount() {
        // Setup
        Set<CategoryModel> categories = new HashSet<>(); // empty set for the failing case

        // Create an instance of ArticleModel with an invalid category count (0 categories)
       //ArticleModel articleModel1 = new ArticleModel(null, "Article Name", "Article Description", 5, 100.5, categories);

        // Validate
        Exception exception = assertThrows(InvalidCategoryCountException.class, () -> {
           // articleModel1.setCategories(categories); // This should throw an exception
        });

        // Assert that the exception message is as expected
        String expectedMessage = "Article must have between 1 and 3 categories";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

        // Logging the expected and actual values
        System.out.println("Expected exception message: " + expectedMessage);
        System.out.println("Actual exception message: " + actualMessage);
    }

}
