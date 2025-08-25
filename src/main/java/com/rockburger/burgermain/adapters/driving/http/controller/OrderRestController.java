package com.rockburger.burgermain.adapters.driving.http.controller;

import com.rockburger.burgermain.adapters.driven.feign.adapter.CartServiceAdapter;
import com.rockburger.burgermain.adapters.driven.feign.dto.CartResponse;
import com.rockburger.burgermain.adapters.driving.http.dto.request.CompleteOrderRequest;
import com.rockburger.burgermain.adapters.driving.http.dto.response.OrderResponse;
import com.rockburger.burgermain.adapters.driving.http.dto.response.ProductAvailabilityResponse;
import com.rockburger.burgermain.adapters.driving.http.dto.response.SalesSummaryResponse;
import com.rockburger.burgermain.adapters.driving.http.mapper.ICartToOrderMapper;
import com.rockburger.burgermain.adapters.driving.http.mapper.IOrderRequestMapper;
import com.rockburger.burgermain.adapters.driving.http.mapper.IOrderResponseMapper;
import com.rockburger.burgermain.domain.api.IOrderServicePort;
import com.rockburger.burgermain.domain.api.IArticleServicePort;
import com.rockburger.burgermain.domain.exception.InsufficientStockException;
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
import java.util.Map;

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
    private final CartServiceAdapter cartServiceAdapter;
    private final ICartToOrderMapper cartToOrderMapper;

    public OrderRestController(IOrderServicePort orderServicePort,
                               IArticleServicePort articleServicePort,
                               IOrderRequestMapper orderRequestMapper,
                               IOrderResponseMapper orderResponseMapper,
                               IUserPersistencePort userPersistencePort,
                               CartServiceAdapter cartServiceAdapter,
                               ICartToOrderMapper cartToOrderMapper) {
        this.orderServicePort = orderServicePort;
        this.articleServicePort = articleServicePort;
        this.orderRequestMapper = orderRequestMapper;
        this.orderResponseMapper = orderResponseMapper;
        this.userPersistencePort = userPersistencePort;
        this.cartServiceAdapter = cartServiceAdapter;
        this.cartToOrderMapper = cartToOrderMapper;
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

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('auxiliar')")
    @Operation(
            summary = "Checkout cart",
            description = "Processes customer cart as a complete order with automatic cart clearing",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Cart checkout completed successfully",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Cart is empty"),
                    @ApiResponse(responseCode = "403", description = "Access denied - only auxiliars can process orders"),
                    @ApiResponse(responseCode = "409", description = "Insufficient stock for one or more items"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<?> checkout(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Starting checkout process for user: {}", userDetails.getUsername());

        try {
            // Get user from authentication
            UserModel user = userPersistencePort.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Fetch active cart
            logger.debug("Fetching active cart from cart service");
            CartResponse cartResponse = cartServiceAdapter.getActiveCart();

            // Validate cart is not empty (AC3: Empty Cart Handling)
            if (cartResponse.getItems() == null || cartResponse.getItems().isEmpty()) {
                logger.warn("Attempted checkout with empty cart for user: {}", userDetails.getUsername());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "CART_EMPTY",
                        "message", "Cannot checkout: cart is empty",
                        "timestamp", java.time.LocalDateTime.now()
                ));
            }

            logger.info("Processing checkout for cart with {} items", cartResponse.getItems().size());

            // Convert cart to order model
            OrderModel orderModel = cartToOrderMapper.toOrderModel(cartResponse, user.getId());

            // Complete the purchase (this handles stock validation and inventory updates)
            OrderModel completedOrder = orderServicePort.completePurchase(orderModel);
            logger.info("Order created successfully with ID: {}", completedOrder.getId());

            // Clear cart after successful order (AC1: Checkout Endpoint Implementation)
            try {
                cartServiceAdapter.clearCart();
                logger.info("Cart cleared successfully after order completion");
            } catch (Exception cartClearException) {
                // AC6: Error Recovery - Order should still be created successfully
                logger.warn("Warning: Failed to clear cart after successful order creation. Order ID: {}, Error: {}",
                        completedOrder.getId(), cartClearException.getMessage());
            }

            // Convert to response
            OrderResponse response = orderResponseMapper.toResponse(completedOrder);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (InsufficientStockException e) {
            // AC2: Business Rules Validation - Handle insufficient stock
            logger.warn("Insufficient stock during checkout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "INSUFFICIENT_STOCK",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            logger.error("Validation error during checkout: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "VALIDATION_ERROR",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (Exception e) {
            // AC4: Transaction Integrity - Proper error response should be returned
            logger.error("Error during checkout process: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "INTERNAL_SERVER_ERROR",
                    "message", "An error occurred during checkout: " + e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
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