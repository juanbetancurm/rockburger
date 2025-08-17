package com.rockburger.burgermain.adapters.driving.http.controller;

import com.rockburger.burgermain.adapters.driving.http.dto.request.AddBrandRequest;
import com.rockburger.burgermain.adapters.driving.http.dto.response.BrandResponse;
import com.rockburger.burgermain.adapters.driving.http.mapper.IBrandRequestMapper;
import com.rockburger.burgermain.adapters.driving.http.mapper.IBrandResponseMapper;
import com.rockburger.burgermain.domain.api.IBrandServicePort;
import com.rockburger.burgermain.domain.model.BrandModel;
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
@RequestMapping("/brand")
@SecurityRequirement(name = "bearer-jwt")
public class BrandRestController {
    private static final Logger logger = LoggerFactory.getLogger(BrandRestController.class);

    private final IBrandServicePort brandServicePort;
    private final IBrandResponseMapper brandResponseMapper;
    private final IBrandRequestMapper brandRequestMapper;

    public BrandRestController(IBrandServicePort brandServicePort,
                               IBrandResponseMapper brandResponseMapper,
                               IBrandRequestMapper brandRequestMapper) {
        this.brandServicePort = brandServicePort;
        this.brandResponseMapper = brandResponseMapper;
        this.brandRequestMapper = brandRequestMapper;
    }

    @PostMapping("/brandnew")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Create a new Brand",
            description = "This endpoint allows you to create a new Brand by providing (MUST) the Brand's name (max length 50 characters) and description (max length 90 characters). " +
                    "If a Brand with the same name already exists, a conflict error will be returned. Only admin users can create brands.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Brand data to be created",
                    content = @Content(
                            schema = @Schema(implementation = AddBrandRequest.class),
                            examples = @ExampleObject(value = "{ \"name\": \"McDonald's\", \"description\": \"Fast food brand\" }")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Brand successfully created",
                            content = @Content(
                                    schema = @Schema(implementation = BrandResponse.class),
                                    examples = @ExampleObject(value = "{ \"id\": 1, \"name\": \"McDonald's\", \"description\": \"Fast food brand\" }")
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation errors occurred"),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin role required"),
                    @ApiResponse(responseCode = "409", description = "Brand name already exists")
            }
    )
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody AddBrandRequest addBrandRequest) {
        try {
            logger.info("Creating new brand: {}", addBrandRequest.getName());

            BrandModel brandModel = brandRequestMapper.addRequestToBrandModel(addBrandRequest);
            BrandModel createdBrand = brandServicePort.createBrand(brandModel);
            BrandResponse brandResponse = brandResponseMapper.toResponse(createdBrand);

            logger.info("Brand created successfully with ID: {}", createdBrand.getId());
            return new ResponseEntity<>(brandResponse, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error creating brand: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create brand", e);
        }
    }

    @GetMapping("/brandspage")
    @PreAuthorize("hasAnyRole('admin', 'auxiliar')")
    @Operation(
            summary = "Get brands with pagination",
            description = "Retrieves a paginated list of brands. Available to both admin and auxiliar users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Brands retrieved successfully",
                            content = @Content(schema = @Schema(implementation = BrandResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin or auxiliar role required")
            }
    )
    public ResponseEntity<List<BrandResponse>> getBrandsWithPagination(
            @Validated @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "true") boolean asc) {

        try {
            logger.info("Getting brands with pagination: page={}, size={}, sortBy={}, asc={}",
                    page, size, sortBy, asc);

            List<BrandModel> brandModels = brandServicePort.getBrandsWithPagination(page, size, sortBy, asc);
            List<BrandResponse> brandResponses = brandResponseMapper.toBrandResponseList(brandModels);

            logger.info("Retrieved {} brands", brandResponses.size());
            return ResponseEntity.ok(brandResponses);

        } catch (Exception e) {
            logger.error("Error retrieving brands: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve brands", e);
        }
    }

    @GetMapping("/brands")
    @PreAuthorize("hasAnyRole('admin', 'auxiliar')")
    @Operation(
            summary = "Get all brands (simplified)",
            description = "Retrieves all brands without pagination. Available to both admin and auxiliar users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Brands retrieved successfully"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        try {
            logger.info("Getting all brands");

            // Use a large page size to get all brands
            List<BrandModel> brandModels = brandServicePort.getBrandsWithPagination(0, 1000, "name", true);
            List<BrandResponse> brandResponses = brandResponseMapper.toBrandResponseList(brandModels);

            logger.info("Retrieved {} brands", brandResponses.size());
            return ResponseEntity.ok(brandResponses);

        } catch (Exception e) {
            logger.error("Error retrieving all brands: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve brands", e);
        }
    }

    @GetMapping("/brands/{id}")
    @PreAuthorize("hasAnyRole('admin', 'auxiliar')")
    @Operation(
            summary = "Get brand by ID",
            description = "Retrieves a specific brand by its ID. Available to both admin and auxiliar users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Brand retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Brand not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        try {
            logger.info("Getting brand by ID: {}", id);

            BrandModel brand = brandServicePort.getBrandById(id);
            BrandResponse response = brandResponseMapper.toResponse(brand);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving brand by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve brand", e);
        }
    }

    @PutMapping("/brands/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Update an existing brand",
            description = "Updates a brand with the provided data. Only admin users can update brands.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Brand updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Brand not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
            }
    )
    public ResponseEntity<BrandResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody AddBrandRequest updateRequest) {

        try {
            logger.info("Updating brand ID: {} with data: {}", id, updateRequest.getName());

            BrandModel brandModel = brandRequestMapper.addRequestToBrandModel(updateRequest);
            brandModel.setId(id);

            BrandModel updatedBrand = brandServicePort.updateBrand(brandModel);
            BrandResponse response = brandResponseMapper.toResponse(updatedBrand);

            logger.info("Brand updated successfully: {}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating brand ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update brand", e);
        }
    }

    @DeleteMapping("/brands/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "Delete a brand",
            description = "Deletes a brand by its ID. Only admin users can delete brands.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Brand deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Brand not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied - admin role required")
            }
    )
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        try {
            logger.info("Deleting brand ID: {}", id);

            brandServicePort.deleteBrand(id);

            logger.info("Brand deleted successfully: {}", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Error deleting brand ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete brand", e);
        }
    }
}