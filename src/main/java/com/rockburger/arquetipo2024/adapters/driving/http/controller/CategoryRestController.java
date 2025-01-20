package com.rockburger.arquetipo2024.adapters.driving.http.controller;



import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddCategoryRequest;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.CategoryResponse;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.ICategoryRequestMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.ICategoryResponseMapper;
import com.rockburger.arquetipo2024.domain.api.ICategoryServicePort;

import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryRestController {

    private final ICategoryServicePort categoryServicePort;

    private final ICategoryResponseMapper categoryResponseMapper;
    private final ICategoryRequestMapper categoryRequestMapper;
    @Autowired
    public CategoryRestController(ICategoryServicePort categoryServicePort,

                                  ICategoryResponseMapper categoryResponseMapper,
                                  ICategoryRequestMapper categoryRequestMapper){
        this.categoryServicePort = categoryServicePort;
        this.categoryResponseMapper = categoryResponseMapper;
        this.categoryRequestMapper = categoryRequestMapper;
    }



    @PostMapping("/categorynew")

    @Operation(
            summary = "Create a new category",
            description = "This endpoint allows you to create a new category by providing (MUST) the category's name (max length 50 characters) and  description (max length 90 characters). If a category with the same name already exists, a conflict error will be returned.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Category data to be created",
                    content = @Content(
                            schema = @Schema(implementation = AddCategoryRequest.class),
                            examples = @ExampleObject(value = "{ \"name\": \"Health\", \"description\": \"Products for human health\" }")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Category successfully created",
                            content = @Content(
                                    schema = @Schema(implementation = CategoryResponse.class),
                                    examples = @ExampleObject(value = "{ \"id\": 1, \"name\": \"Health\", \"description\": \"Products for human health\" }")
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation errors occurred",
                            content = @Content(
                                    schema = @Schema(implementation = CategoryResponse.class),
                                    examples = @ExampleObject(value = "{ \"id\": 1, \"   \": \"     \", \"description\": \"Any description.\" }")
                            )
                    ),
                    @ApiResponse(responseCode = "409", description = "Category already exists",
                            content = @Content(
                                    schema = @Schema(implementation = CategoryResponse.class),
                                    examples = @ExampleObject(value = "{ \"id\": 1, \"   \": \"A name that already exists \", \"description\": \"Any description.\" }")
                            )),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )

    public ResponseEntity<CategoryResponse> createCategory(@Validated @RequestBody AddCategoryRequest addCategoryRequest) {
        CategoryModel categoryModel = categoryRequestMapper.addRequestToCategoryModel(addCategoryRequest);
        CategoryModel createdCategory = categoryServicePort.createCategory(categoryModel);
        CategoryResponse categoryResponse = categoryResponseMapper.toResponse(createdCategory);
        return new ResponseEntity<>(categoryResponse, HttpStatus.CREATED);
    }

    @GetMapping("/categoriespage")
    public ResponseEntity<List<CategoryResponse>> getCategoriesWithPagination(@Validated
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "true") boolean asc){
        List<CategoryModel> categoryModels = categoryServicePort.getCategoriesWithPagination(page, size, sortBy, asc);
        List<CategoryResponse>categoryResponses = categoryResponseMapper.toCategoryResponseList(categoryModels);
        return ResponseEntity.ok(categoryResponses);
    }
}
