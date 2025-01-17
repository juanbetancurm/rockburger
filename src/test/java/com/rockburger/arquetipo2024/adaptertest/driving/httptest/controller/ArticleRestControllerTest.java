package com.rockburger.arquetipo2024.adaptertest.driving.httptest.controller;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

import com.rockburger.arquetipo2024.adapters.driving.http.controller.ArticleRestController;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddArticleRequest;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.ArticleResponse;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IArticleRequestMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IArticleResponseMapper;
import com.rockburger.arquetipo2024.domain.api.IArticleServicePort;
import com.rockburger.arquetipo2024.domain.api.IBrandServicePort;
import com.rockburger.arquetipo2024.domain.api.ICategoryServicePort;
import com.rockburger.arquetipo2024.domain.exception.CategoryNotFoundException;
import com.rockburger.arquetipo2024.domain.exception.NameAlreadyExistsExceptionD;
import com.rockburger.arquetipo2024.domain.exception.NotFoundException;
import com.rockburger.arquetipo2024.domain.model.ArticleModel;
import com.rockburger.arquetipo2024.domain.model.BrandModel;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ArticleRestController.class)
class ArticleRestControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(ArticleRestControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IArticleServicePort articleServicePort;

    @MockBean
    private IArticleRequestMapper articleRequestMapper;

    @MockBean
    private IArticleResponseMapper articleResponseMapper;

    @MockBean
    private ICategoryServicePort categoryServicePort;

    @MockBean
    private IBrandServicePort brandServicePort;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testAddArticle_Success() throws Exception {
        // Arrange
        AddArticleRequest request = new AddArticleRequest(
                "Test Article", "Test Description", 10, 100.0, Set.of(1L, 2L), 1L);

        ArticleModel articleModel = new ArticleModel();
        articleModel.setId(1L);
        articleModel.setName("Test Article");
        articleModel.setDescription("Test Description");
        articleModel.setQuantity(10);
        articleModel.setPrice(100.0);

        Set<CategoryModel> categories = new HashSet<>();
        categories.add(new CategoryModel(1L, "Category 1", "Description 1"));
        categories.add(new CategoryModel(2L, "Category 2", "Description 2"));
        articleModel.setCategories(categories);

        BrandModel brandModel = new BrandModel(1L, "Test Brand", "Description Brand");
        articleModel.setBrand(brandModel);

        ArticleResponse response = new ArticleResponse(
                1L, "Test Article", "Test Description", 10, 100.0, null, 1L, "Test Brand", null);

        logger.info("Request: {}", request);
        logger.info("Expected ArticleModel: {}", articleModel);
        logger.info("Expected Response: {}", response);

        when(articleRequestMapper.toModel(any(AddArticleRequest.class))).thenReturn(articleModel);
        when(categoryServicePort.getCategoryById(1L)).thenReturn(categories.iterator().next());
        when(categoryServicePort.getCategoryById(2L)).thenReturn(categories.iterator().next());
        when(brandServicePort.getBrandById(1L)).thenReturn(brandModel);
        when(articleServicePort.createNewArticle(any(ArticleModel.class))).thenReturn(articleModel);
        when(articleResponseMapper.toResponse(any(ArticleModel.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/article/newarticle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Article"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.price").value(100.0))
                .andExpect(jsonPath("$.brandId").value(1L))
                .andExpect(jsonPath("$.brandName").value("Test Brand"))
                .andDo(result -> logger.info("Response: {}", result.getResponse().getContentAsString()));
    }

    @Test
    void testAddArticle_InvalidRequest() throws Exception {
        // Arrange
        AddArticleRequest request = new AddArticleRequest(
                "", "", 0, -1.0, null, null);

        logger.info("Invalid Request: {}", request);

        // Act & Assert
        mockMvc.perform(post("/article/newarticle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name is required"))
                .andExpect(jsonPath("$.description").value("Description is required"))
                .andExpect(jsonPath("$.quantity").value("Quantity must be at least 1"))
                .andExpect(jsonPath("$.price").value("Price must be greater than 0"))
                .andExpect(jsonPath("$.categoryIds").value("Categories Cannot Be Null"))
                .andExpect(jsonPath("$.brandId").value("Brand ID Cannot Be Null"))
                .andDo(result -> logger.info("Response: {}", result.getResponse().getContentAsString()));
    }

    @Test
    void testAddArticle_CategoryNotFound() throws Exception {
        // Arrange
        AddArticleRequest request = new AddArticleRequest(
                "Test Article", "Test Description", 10, 100.0, Set.of(1L, 2L), 1L);

        logger.info("Request: {}", request);
        CategoryModel category1 = new CategoryModel(1L, "Category 1", "Description for Category 1");
        CategoryModel category2 = new CategoryModel(2L, "Category 2", "Description for Category 2");

        // Mock the articleRequestMapper to return a valid ArticleModel
        ArticleModel articleModel = new ArticleModel();
        articleModel.setName("Test Article");
        articleModel.setDescription("Test Description");
        articleModel.setQuantity(10);
        articleModel.setPrice(100.0);
        //articleModel.setCategories(new HashSet<>());  // Empty set initially
        articleModel.setCategories(new HashSet<>(Set.of(category1, category2)));

        // Mock the behavior of the articleRequestMapper
        when(articleRequestMapper.toModel(any(AddArticleRequest.class))).thenReturn(articleModel);

        // Mock the categoryServicePort to throw an exception for a non-existent category
        when(categoryServicePort.getCategoryById(1L)).thenThrow(new CategoryNotFoundException("Category not found"));
        when(categoryServicePort.getCategoryById(2L)).thenReturn(new CategoryModel(2L, "Category 2", "Description for Category 2")); // Assume 2L is valid

        // Act & Assert
        mockMvc.perform(post("/article/newarticle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"))
                .andDo(result -> logger.info("Response: {}", result.getResponse().getContentAsString()));
    }


    @Test
    void testAddArticle_BrandNotFound() throws Exception {
        // Arrange
        AddArticleRequest request = new AddArticleRequest(
                "Test Article", "Test Description", 10, 100.0, Set.of(1L, 2L), 1L);

        logger.info("Request: {}", request);

        when(categoryServicePort.getCategoryById(any())).thenReturn(new CategoryModel(1L, "Category 1", "Description 1"));
        when(brandServicePort.getBrandById(1L)).thenThrow(new NotFoundException("Brand not found"));

        // Act & Assert
        mockMvc.perform(post("/article/newarticle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Brand not found"))
                .andDo(result -> logger.info("Response: {}", result.getResponse().getContentAsString()));
    }

    @Test
    void testAddArticle_DuplicateName() throws Exception {
        // Arrange
        AddArticleRequest request = new AddArticleRequest(
                "Existing Article", "Test Description", 10, 100.0, Set.of(1L, 2L), 1L);

        logger.info("Request: {}", request);

        when(categoryServicePort.getCategoryById(any())).thenReturn(new CategoryModel(1L, "Category 1", "Description 1"));
        when(brandServicePort.getBrandById(any())).thenReturn(new BrandModel(1L, "Test Brand", "Description Brand"));
        when(articleServicePort.createNewArticle(any())).thenThrow(new NameAlreadyExistsExceptionD("Article with name 'Existing Article' already exists"));

        // Act & Assert
        mockMvc.perform(post("/article/newarticle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Article with name 'Existing Article' already exists"))
                .andDo(result -> logger.info("Response: {}", result.getResponse().getContentAsString()));
    }
}