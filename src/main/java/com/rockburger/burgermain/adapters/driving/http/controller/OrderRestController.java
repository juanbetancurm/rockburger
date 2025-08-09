package com.rockburger.burgermain.adapters.driving.http.controller;

import com.rockburger.burgermain.adapters.driving.http.dto.request.CompleteOrderRequest;
import com.rockburger.burgermain.adapters.driving.http.dto.response.OrderResponse;
import com.rockburger.burgermain.adapters.driving.http.dto.response.ProductAvailabilityResponse;
import com.rockburger.burgermain.adapters.driving.http.dto.response.SalesSummaryResponse;
import com.rockburger.burgermain.adapters.driving.http.mapper.IOrderRequestMapper;
import com.rockburger.burgermain.adapters.driving.http.mapper.IOrderResponseMapper;
import com.rockburger.burgermain.domain.api.IOrderServicePort;
import com.rockburger.burgermain.domain.api.IArticleServicePort;
import com.rockburger.burgermain.domain.model.ArticleModel;
import com.rockburger.burgermain.domain.model.OrderModel;
import com.rockburger.burgermain.domain.model.SalesSummaryModel;
import com.rockburger.burgermain.domain.model.UserModel;
import com.rockburger.burgermain.domain.spi.IUserPersistencePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/purchase")
@SecurityRequirement(name = "bearer-jwt")
public class OrderRestController {
    private static final Logger logger = LoggerFactory.getLogger(OrderRestController.class);

    private final IOrderServicePort orderServicePort;
    private final IArticleServicePort articleServicePort;
    private final IOrderRequestMapper orderRequestMapper;
    private final IOrderResponseMapper orderResponseMapper;
    private final IUserPersistencePort userPersistencePort;

    public OrderRestController(IOrderServicePort orderServicePort,
                               IArticleServicePort articleServicePort,
                               IOrderRequestMapper orderRequestMapper,
                               IOrderResponseMapper orderResponseMapper,
                               IUserPersistencePort userPersistencePort) {
        this.orderServicePort = orderServicePort;
        this.articleServicePort = articleServicePort;
        this.orderRequestMapper = orderRequestMapper;
        this.orderResponseMapper = orderResponseMapper;
        this.userPersistencePort = userPersistencePort;
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('auxiliar')")
    @Operation(
            summary = "Complete a purchase",
            description = "Completes a purchase by saving the order and updating inventory",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Purchase completed successfully",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<OrderResponse> completePurchase(
            @Valid @RequestBody CompleteOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("Completing purchase for user: {}", userDetails.getUsername());

        // Get user from authentication
        UserModel user = userPersistencePort.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Convert request to domain model
        OrderModel orderModel = orderRequestMapper.toModel(request, user.getId());

        // Complete the purchase
        OrderModel completedOrder = orderServicePort.completePurchase(orderModel);

        // Convert to response
        OrderResponse response = orderResponseMapper.toResponse(completedOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/availability")
    @PreAuthorize("hasRole('auxiliar')")
    @Operation(
            summary = "Get product availability",
            description = "Returns current stock levels for all products",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product availability retrieved successfully"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<List<ProductAvailabilityResponse>> getProductAvailability() {
        logger.info("Getting product availability");

        List<ArticleModel> articles = articleServicePort.listArticles("name", "asc", 0, 100);
        List<ProductAvailabilityResponse> availability = orderResponseMapper.toAvailabilityResponseList(articles);

        return ResponseEntity.ok(availability);
    }

    @GetMapping("/sales/daily")
    @PreAuthorize("hasRole('auxiliar')")
    @Operation(
            summary = "Get daily sales summary",
            description = "Returns total orders and revenue for a specific date",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sales summary retrieved successfully",
                            content = @Content(schema = @Schema(implementation = SalesSummaryResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    public ResponseEntity<SalesSummaryResponse> getDailySalesSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate summaryDate = date != null ? date : LocalDate.now();
        logger.info("Getting daily sales summary for date: {}", summaryDate);

        SalesSummaryModel summary = orderServicePort.getDailySalesSummary(summaryDate);
        SalesSummaryResponse response = orderResponseMapper.toResponse(summary);

        return ResponseEntity.ok(response);
    }
}
