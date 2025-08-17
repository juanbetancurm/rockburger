package com.rockburger.burgermain.adapters.driving.http.controller;

import com.rockburger.burgermain.adapters.driving.http.dto.request.AddCategoryRequest;
import com.rockburger.burgermain.adapters.driving.http.dto.response.CategoryResponse;
import com.rockburger.burgermain.adapters.driving.http.mapper.ICategoryRequestMapper;
import com.rockburger.burgermain.adapters.driving.http.mapper.ICategoryResponseMapper;
import com.rockburger.burgermain.domain.api.ICategoryServicePort;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/category")
@SecurityRequirement(name = "bearer-jwt")
public class CategoryRestController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryRestController.class);

    private final ICategoryServicePort categoryServicePort;
    private final ICategoryResponseMapper categoryResponseMapper;
    private final ICategoryRequestMapper categoryRequestMapper;

    public CategoryRestController(ICategoryServicePort categoryServicePort,
                                  ICategoryResponseMapper categoryResponseMapper,
                                  ICategoryRequestMapper categoryRequestMapper) {
        this.categoryServicePort = categoryServicePort;
        this.categoryResponseMapper = categoryResponseMapper;
        this.categoryRequestMapper = categoryRequestMapper;
    }

    @PostMapping("/categorynew")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Create a new category",
            description = "This endpoint allows you to create a new category by providing (MUST) the category's name (max length 50 characters) and description (max length 90 characters). " +
                    "If a category with the same name already exists, a conflict error will be returned. Only admin users can create categories.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Category data to be created",
                    content = @Content(
                            schema = @Schema(implementation = AddCategoryRequest.class),
                            examples = @ExampleObject(value = "{ \"name\": \"Burgers\", \"description\": \"Delicious burger category\" }")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Category successfully created",
                            content = @Content(
                                    schema = @Schema(implementation = CategoryResponse.class),
                                    examples = @ExampleObject(value = "{ \"id\": 1, \"name\": \"Burgers\", \"description\": \"Delicious burger category\" }")
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation errors occurred"),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
                    @ApiResponse(responseCode = "409", description = "Category name already exists")
            }
    )
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody AddCategoryRequest addCategoryRequest) {
        try {
            logger.info("Creating new category: {}", addCategoryRequest.getName());

            CategoryModel categoryModel = categoryRequestMapper.addRequestToCategoryModel(addCategoryRequest);
            CategoryModel createdCategory = categoryServicePort.createCategory(categoryModel);
            CategoryResponse categoryResponse = categoryResponseMapper.toResponse(createdCategory);

            logger.info("Category created successfully with ID: {}", createdCategory.getId());
            return new ResponseEntity<>(categoryResponse, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error creating category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create category", e);
        }
    }

    @GetMapping("/categoriespage")
    @PreAuthorize("hasAnyRole('admin', 'auxiliar')")
    @Operation(
            summary = "Get categories with pagination",
            description = "Retrieves a paginated list of categories. Available to both admin and auxiliar users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                            content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin or auxiliar role required")
            }
    )
    public ResponseEntity<List<CategoryResponse>> getCategoriesWithPagination(
            @Validated @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "true") boolean asc) {

        try {
            logger.info("Getting categories with pagination: page={}, size={}, sortBy={}, asc={}",
                    page, size, sortBy, asc);

            List<CategoryModel> categoryModels = categoryServicePort.getCategoriesWithPagination(page, size, sortBy, asc);
            List<CategoryResponse> categoryResponses = categoryResponseMapper.toCategoryResponseList(categoryModels);

            logger.info("Retrieved {} categories", categoryResponses.size());
            return ResponseEntity.ok(categoryResponses);

        } catch (Exception e) {
            logger.error("Error retrieving categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve categories", e);
        }
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('admin', 'auxiliar')")
    @Operation(
            summary = "Get all categories (simplified)",
            description = "Retrieves all categories without pagination. Available to both admin and auxiliar users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        try {
            logger.info("Getting all categories");

            // Use a large page size to get all categories
            List<CategoryModel> categoryModels = categoryServicePort.getCategoriesWithPagination(0, 1000, "name", true);
            List<CategoryResponse> categoryResponses = categoryResponseMapper.toCategoryResponseList(categoryModels);

            logger.info("Retrieved {} categories", categoryResponses.size());
            return ResponseEntity.ok(categoryResponses);

        } catch (Exception e) {
            logger.error("Error retrieving all categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve categories", e);
        }
    }

    @GetMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('admin', 'auxiliar')")
    @Operation(
            summary = "Get category by ID",
            description = "Retrieves a specific category by its ID. Available to both admin and auxiliar users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Category not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        try {
            logger.info("Getting category by ID: {}", id);

            CategoryModel category = categoryServicePort.getCategoryById(id);
            CategoryResponse response = categoryResponseMapper.toResponse(category);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving category by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve category", e);
        }
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Update an existing category",
            description = "Updates a category with the provided data. Only admin users can update categories.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Category not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
            }
    )
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody AddCategoryRequest updateRequest) {

        try {
            logger.info("Updating category ID: {} with data: {}", id, updateRequest.getName());

            CategoryModel categoryModel = categoryRequestMapper.addRequestToCategoryModel(updateRequest);
            categoryModel.setId(id);

            CategoryModel updatedCategory = categoryServicePort.updateCategory(categoryModel);
            CategoryResponse response = categoryResponseMapper.toResponse(updatedCategory);

            logger.info("Category updated successfully: {}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating category ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update category", e);
        }
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Delete a category",
            description = "Deletes a category by its ID. Only admin users can delete categories.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Category not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
            }
    )
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            logger.info("Deleting category ID: {}", id);

            categoryServicePort.deleteCategory(id);

            logger.info("Category deleted successfully: {}", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Error deleting category ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete category", e);
        }
    }
}