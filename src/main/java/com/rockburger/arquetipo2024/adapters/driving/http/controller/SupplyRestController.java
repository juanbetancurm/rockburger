package com.rockburger.arquetipo2024.adapters.driving.http.controller;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddSupplyRequest;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.SupplyResponse;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.ISupplyRequestMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.ISupplyResponseMapper;
import com.rockburger.arquetipo2024.domain.api.IArticleServicePort;
import com.rockburger.arquetipo2024.domain.api.ISupplyServicePort;
import com.rockburger.arquetipo2024.domain.model.ArticleModel;
import com.rockburger.arquetipo2024.domain.model.SupplyModel;
import com.rockburger.arquetipo2024.domain.model.UserModel;
import com.rockburger.arquetipo2024.domain.spi.IUserPersistencePort;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/supply")
@SecurityRequirement(name = "bearer-jwt")
public class SupplyRestController {
    private static final Logger logger = LoggerFactory.getLogger(SupplyRestController.class);

    private final ISupplyServicePort supplyServicePort;
    private final IArticleServicePort articleServicePort;
    private final ISupplyRequestMapper supplyRequestMapper;
    private final ISupplyResponseMapper supplyResponseMapper;
    private final IUserPersistencePort userPersistencePort;

    public SupplyRestController(
            ISupplyServicePort supplyServicePort,
            IArticleServicePort articleServicePort,
            ISupplyRequestMapper supplyRequestMapper,
            ISupplyResponseMapper supplyResponseMapper,
            IUserPersistencePort userPersistencePort) {
        this.supplyServicePort = supplyServicePort;
        this.articleServicePort = articleServicePort;
        this.supplyRequestMapper = supplyRequestMapper;
        this.supplyResponseMapper = supplyResponseMapper;
        this.userPersistencePort = userPersistencePort;

    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('auxiliar')")
    @Operation(
            summary = "Add supplies to an article",
            description = "Allows warehouse assistants to add supplies to existing articles. " +
                    "Updates stock levels and maintains transaction safety. " +
                    "Requires authentication with auxiliar role.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddSupplyRequest.class),
                            examples = @ExampleObject(
                                    name = "Add Supply Example",
                                    summary = "Example of adding 10 units to article ID 2",
                                    value = """
                    {
                        "articleId": 2,
                        "quantity": 10
                    }
                    """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Supply successfully added",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SupplyResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "id": 1,
                            "articleId": 2,
                            "articleName": "CarneMixta",
                            "quantity": 10,
                            "supplyDate": "2025-01-26T14:30:00",
                            "supplierEmail": "juan.auxiliar@rockburger.com"
                        }
                        """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "message": "Quantity must be greater than 0",
                            "status": "BAD_REQUEST"
                        }
                        """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Article not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "message": "Article not found with ID: 2",
                            "status": "NOT_FOUND"
                        }
                        """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Insufficient permissions",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "message": "Access Denied",
                            "status": "FORBIDDEN"
                        }
                        """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Concurrent modification detected",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "message": "Concurrent modification detected",
                            "status": "CONFLICT"
                        }
                        """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<SupplyResponse> addSupply(
            @Valid @RequestBody AddSupplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("Received supply addition request for article ID: {}", request.getArticleId());

        try {
            // Convert request to domain model
            SupplyModel supplyModel = supplyRequestMapper.toModel(request);

            // Set the authenticated user as supplier
            UserModel supplier = userPersistencePort.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
            supplyModel.setSupplier(supplier);

            // Get and set the article
            ArticleModel article = articleServicePort.getArticleById(request.getArticleId());
            supplyModel.setArticle(article);

            // Process the supply addition
            SupplyModel savedSupply = supplyServicePort.addSupply(supplyModel);

            // Convert to response DTO
            SupplyResponse response = supplyResponseMapper.toResponse(savedSupply);

            logger.info("Successfully processed supply addition for article ID: {}",
                    request.getArticleId());

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error processing supply addition: ", e);
            throw e;
        }
    }
}