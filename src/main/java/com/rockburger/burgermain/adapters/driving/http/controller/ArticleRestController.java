package com.rockburger.burgermain.adapters.driving.http.controller;

import com.rockburger.burgermain.adapters.driving.http.dto.request.AddArticleRequest;
import com.rockburger.burgermain.adapters.driving.http.dto.response.ArticleResponse;
import com.rockburger.burgermain.adapters.driving.http.mapper.IArticleRequestMapper;
import com.rockburger.burgermain.adapters.driving.http.mapper.IArticleResponseMapper;
import com.rockburger.burgermain.configuration.exceptionhandler.ErrorResponse;
import com.rockburger.burgermain.domain.api.IArticleServicePort;
import com.rockburger.burgermain.domain.api.IBrandServicePort;
import com.rockburger.burgermain.domain.api.ICategoryServicePort;
import com.rockburger.burgermain.domain.exception.BlankFieldException;
import com.rockburger.burgermain.domain.model.ArticleModel;
import com.rockburger.burgermain.domain.model.BrandModel;
import com.rockburger.burgermain.domain.model.CategoryModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/article")
@SecurityRequirement(name = "bearer-jwt")
public class ArticleRestController {
    private static final Logger logger = LoggerFactory.getLogger(ArticleRestController.class);

    private final IArticleServicePort articleServicePort;
    private final IArticleRequestMapper articleRequestMapper;
    private final IArticleResponseMapper articleResponseMapper;
    private final ICategoryServicePort categoryServicePort;
    private final IBrandServicePort brandServicePort;

    public ArticleRestController(IArticleServicePort articleServicePort,
                                 IArticleRequestMapper articleRequestMapper,
                                 IArticleResponseMapper articleResponseMapper,
                                 ICategoryServicePort categoryServicePort,
                                 IBrandServicePort brandServicePort) {
        this.articleServicePort = articleServicePort;
        this.articleRequestMapper = articleRequestMapper;
        this.articleResponseMapper = articleResponseMapper;
        this.categoryServicePort = categoryServicePort;
        this.brandServicePort = brandServicePort;
    }

    @PostMapping("/newarticle")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Create a new Article",
            description = "Here you can create a new Article by providing the article's name (max length 50 characters), description (max length 90 characters), " +
                    "quantity, price, and a list of category IDs (between 1 and 3 categories). " +
                    "If an article with the same name already exists or validation fails, an appropriate error will be returned.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Article data to be created",
                    content = @Content(
                            schema = @Schema(implementation = AddArticleRequest.class),
                            examples = @ExampleObject(value = "{ \"name\": \"Test Article\", \"description\": \"Test Description\", \"quantity\": 10, \"price\": 100.0, \"categoryIds\": [1, 2], \"brandId\": 1 }")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Article successfully created",
                            content = @Content(
                                    schema = @Schema(implementation = ArticleResponse.class),
                                    examples = @ExampleObject(value = "{ \"id\": 1, \"name\": \"Test Article\", \"description\": \"Test Description\", \"quantity\": 10, \"price\": 100.0, " +
                                            "\"categories\": [{ \"id\": 1, \"name\": \"Category 1\" }, { \"id\": 2, \"name\": \"Category 2\" }] }")
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation errors occurred",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = "{ \"message\": \"Categories Cannot Be Null\", \"status\": \"400 BAD_REQUEST\", \"timestamp\": \"2024-09-12T16:39:22.6288153\" }")
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
                    @ApiResponse(responseCode = "409", description = "Article name already exists")
            }
    )
    public ResponseEntity<ArticleResponse> createArticle(@Valid @RequestBody AddArticleRequest addArticleRequest) {
        try {
            logger.info("Creating new article: {}", addArticleRequest.getName());

            if (addArticleRequest.getCategoryIds() == null || addArticleRequest.getCategoryIds().isEmpty()) {
                throw new BlankFieldException("Categories Cannot Be Null");
            }

            ArticleModel articleModel = articleRequestMapper.toModel(addArticleRequest);
            logger.info("Initial categories in Controller: {}", articleModel.getCategories());

            Set<CategoryModel> categoryModels = addArticleRequest.getCategoryIds().stream()
                    .map(categoryServicePort::getCategoryById)
                    .collect(Collectors.toSet());
            articleModel.setCategories(categoryModels);

            logger.info("getBrandById: {}", addArticleRequest.getBrandId());
            BrandModel brandModel = brandServicePort.getBrandById(addArticleRequest.getBrandId());
            logger.info("BrandModel: {}", brandModel);
            articleModel.setBrand(brandModel);
            logger.info("setBrand: {}", articleModel);

            ArticleModel createdArticle = articleServicePort.createNewArticle(articleModel);
            logger.info("ArticleModel after saving: {}", createdArticle);
            logger.info("ArticleModel brandId after saving: {}", createdArticle.getBrand().getId());

            ArticleResponse response = articleResponseMapper.toResponse(createdArticle);
            logger.info("Received articleModel: {}", response);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (BlankFieldException e) {
            logger.error("Validation error creating article: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating article: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create article", e);
        }
    }

    @GetMapping("/articles")
    @PreAuthorize("hasAnyRole('admin', 'auxiliar')")
    @Operation(
            summary = "Get articles with pagination",
            description = "Retrieves a paginated list of articles. Available to both admin and auxiliar users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Articles retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ArticleResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin or auxiliar role required")
            }
    )
    public ResponseEntity<List<ArticleResponse>> getArticlesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        try {
            logger.info("Getting articles with pagination: page={}, size={}, sortBy={}, sortOrder={}",
                    page, size, sortBy, sortOrder);

            List<ArticleModel> articles = articleServicePort.listArticles(sortBy, sortOrder, page, size);
            List<ArticleResponse> articleResponses = articleResponseMapper.toArticleResponseList(articles);

            logger.info("Retrieved {} articles", articleResponses.size());
            return ResponseEntity.ok(articleResponses);

        } catch (Exception e) {
            logger.error("Error retrieving articles: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve articles", e);
        }
    }

    @GetMapping("/articles/{id}")
    @PreAuthorize("hasAnyRole('admin', 'auxiliar')")
    @Operation(
            summary = "Get article by ID",
            description = "Retrieves a specific article by its ID. Available to both admin and auxiliar users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Article retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Article not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<ArticleResponse> getArticleById(@PathVariable Long id) {
        try {
            logger.info("Getting article by ID: {}", id);

            ArticleModel article = articleServicePort.getArticleById(id);
            ArticleResponse response = articleResponseMapper.toResponse(article);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving article by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve article", e);
        }
    }

    @PutMapping("/articles/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Update an existing article",
            description = "Updates an article with the provided data. Only admin users can update articles.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Article updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Article not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
            }
    )
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody AddArticleRequest updateRequest) {

        try {
            logger.info("Updating article ID: {} with data: {}", id, updateRequest.getName());

            ArticleModel articleModel = articleRequestMapper.toModel(updateRequest);
            articleModel.setId(id);

            if (updateRequest.getCategoryIds() != null && !updateRequest.getCategoryIds().isEmpty()) {
                Set<CategoryModel> categoryModels = updateRequest.getCategoryIds().stream()
                        .map(categoryServicePort::getCategoryById)
                        .collect(Collectors.toSet());
                articleModel.setCategories(categoryModels);
            }

            if (updateRequest.getBrandId() != null) {
                BrandModel brandModel = brandServicePort.getBrandById(updateRequest.getBrandId());
                articleModel.setBrand(brandModel);
            }

            ArticleModel updatedArticle = articleServicePort.updateArticle(articleModel);
            ArticleResponse response = articleResponseMapper.toResponse(updatedArticle);

            logger.info("Article updated successfully: {}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating article ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update article", e);
        }
    }

    @DeleteMapping("/articles/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Delete an article",
            description = "Deletes an article by its ID. Only admin users can delete articles.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Article deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Article not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
            }
    )
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        try {
            logger.info("Deleting article ID: {}", id);

            articleServicePort.deleteArticle(id);

            logger.info("Article deleted successfully: {}", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Error deleting article ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete article", e);
        }
    }
}