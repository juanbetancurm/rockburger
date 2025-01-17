package com.rockburger.arquetipo2024.adapters.driving.http.controller;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddBrandRequest;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.BrandResponse;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IBrandRequestMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IBrandResponseMapper;
import com.rockburger.arquetipo2024.domain.api.IBrandServicePort;
import com.rockburger.arquetipo2024.domain.model.BrandModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandRestController {
    private static final Logger logger = LoggerFactory.getLogger(BrandRestController.class);
    private final IBrandServicePort brandServicePort;

    private final IBrandResponseMapper brandResponseMapper;
    private final IBrandRequestMapper brandRequestMapper;
    @Autowired
    public BrandRestController(IBrandServicePort brandServicePort,
                               IBrandResponseMapper brandResponseMapper,
                               IBrandRequestMapper brandRequestMapper){
        this.brandServicePort = brandServicePort;
        this.brandResponseMapper = brandResponseMapper;
        this.brandRequestMapper = brandRequestMapper;
    }
    @PostMapping("/brandnew")

    @Operation(
            summary = "Create a new Brand",
            description = "This endpoint allows you to create a new Brand by providing (MUST) the Brand's name (max length 50 characters) and  description (max length 90 characters). If a Brand with the same name already exists, a conflict error will be returned.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Brand data to be created",
                    content = @Content(
                            schema = @Schema(implementation = AddBrandRequest.class),
                            examples = @ExampleObject(value = "{ \"name\": \"Zodiac\", \"description\": \"Colombian Shoes\" }")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Brand successfully created",
                            content = @Content(
                                    schema = @Schema(implementation = BrandResponse.class),
                                    examples = @ExampleObject(value = "{ \"id\": 1, \"name\": \"Adidaz\", \"description\": \"Colombian Shoes\" }")
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation errors occurred",
                            content = @Content(
                                    schema = @Schema(implementation = BrandResponse.class),
                                    examples = @ExampleObject(value = "{ \"id\": 1, \"   \": \"     \", \"description\": \"Any description.\" }")
                            )
                    ),
                    @ApiResponse(responseCode = "409", description = "Brand already exists",
                            content = @Content(
                                    schema = @Schema(implementation = BrandResponse.class),
                                    examples = @ExampleObject(value = "{ \"id\": 1, \"   \": \"A name that already exists \", \"description\": \"Any description.\" }")
                            )),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )

    public ResponseEntity<BrandResponse> createBrand(@Validated @RequestBody AddBrandRequest addBrandRequest) {
        BrandModel brandModel = brandRequestMapper.addRequestToBrandModel(addBrandRequest);
        BrandModel createdBrand = brandServicePort.createBrand(brandModel);
        BrandResponse brandResponse = brandResponseMapper.toResponse(createdBrand);
        return new ResponseEntity<>(brandResponse, HttpStatus.CREATED);
    }

    @GetMapping("/brandspage")
    public ResponseEntity<List<BrandResponse>> getCategoriesWithPagination(@Validated
                                                                              @RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "10") int size,
                                                                              @RequestParam(defaultValue = "name") String sortBy,
                                                                              @RequestParam(defaultValue = "true") boolean asc){
        List<BrandModel> brandModels = brandServicePort.getBrandsWithPagination(page, size, sortBy, asc);
        List<BrandResponse> brandResponses = brandResponseMapper.toBrandResponseList(brandModels);
        return ResponseEntity.ok(brandResponses);
    }
}
