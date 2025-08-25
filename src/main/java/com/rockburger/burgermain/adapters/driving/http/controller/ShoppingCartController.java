package com.rockburger.burgermain.adapters.driving.http.controller;

import com.rockburger.burgermain.adapters.driven.feign.dto.CartResponse;
import com.rockburger.burgermain.adapters.driven.feign.facade.CartFacadeService;
import com.rockburger.burgermain.domain.api.IArticleServicePort;
import com.rockburger.burgermain.domain.exception.DuplicateCartItemException;
import com.rockburger.burgermain.domain.model.ArticleModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shopping-cart")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasAnyRole('client', 'auxiliar')")
@RequiredArgsConstructor
public class ShoppingCartController {
    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartController.class);

    private final CartFacadeService cartFacadeService;
    private final IArticleServicePort articleServicePort;

    @GetMapping
    @Operation(summary = "Get active cart", description = "Retrieves the current user's active cart")
    @ApiResponse(responseCode = "200", description = "Cart retrieved successfully")
    public ResponseEntity<CartResponse> getCart() {
        logger.info("Retrieving active cart");
        return ResponseEntity.ok(cartFacadeService.getActiveCart());
    }

    @PostMapping("/items/{articleId}")
    @Operation(summary = "Add item to cart", description = "Adds an article to the cart with specified quantity")
    @ApiResponse(responseCode = "200", description = "Item added successfully")
    public ResponseEntity<CartResponse> addToCart(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") int quantity) {

        // ADD THIS LOGGING BLOCK
        logger.info("=== SHOPPING CART CONTROLLER DEBUG ===");
        logger.info("Adding article {} to cart, quantity: {}", articleId, quantity);
        logger.info("Thread ID: {}", Thread.currentThread().getId());
        logger.info("Thread Name: {}", Thread.currentThread().getName());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("SecurityContext has authentication: {}", auth != null);
        if (auth != null) {
            logger.info("Auth credentials available: {}", auth.getCredentials() != null);
            logger.info("Auth credentials is String: {}", auth.getCredentials() instanceof String);
            if (auth.getCredentials() instanceof String) {
                String token = (String) auth.getCredentials();
                logger.info("Token length: {}, starts with eyJ: {}", token.length(), token.startsWith("eyJ"));
            }
        }
        logger.info("=== END SHOPPING CART CONTROLLER DEBUG ===");

        try {
            // Get the article details
            ArticleModel article = articleServicePort.getArticleById(articleId);

            // Add to cart
            CartResponse cart = cartFacadeService.addArticleToCart(article, quantity);

            return ResponseEntity.ok(cart);

        } catch (DuplicateCartItemException e) {
            // Handle duplicate item gracefully
            logger.info("Handling duplicate item for articleId: {}", e.getArticleId());

            // Return current cart state with a warning header
            try {
                CartResponse currentCart = cartFacadeService.getActiveCart();

                // Add custom header to indicate the item already exists
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .header("X-Cart-Warning", "Item already exists in cart")
                        .header("X-Duplicate-Article-Id", String.valueOf(e.getArticleId()))
                        .body(currentCart);

            } catch (Exception fallbackEx) {
                logger.error("Failed to get current cart after duplicate detection: {}", fallbackEx.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .header("X-Cart-Warning", "Item already exists in cart")
                        .build();
            }
        }
    }

    @DeleteMapping("/items/{articleId}")
    @Operation(summary = "Remove item from cart", description = "Removes an article from the cart")
    @ApiResponse(responseCode = "200", description = "Item removed successfully")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable Long articleId) {
        logger.info("Removing article {} from cart", articleId);
        return ResponseEntity.ok(cartFacadeService.removeArticleFromCart(articleId));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the cart")
    @ApiResponse(responseCode = "204", description = "Cart cleared successfully")
    public ResponseEntity<Void> clearCart() {
        logger.info("Clearing cart");
        cartFacadeService.clearCart();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Get cart summary", description = "Returns a summary of the cart")
    @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    public ResponseEntity<CartSummary> getCartSummary() {
        logger.info("Getting cart summary");

        int itemCount = cartFacadeService.getCartItemCount();
        double total = cartFacadeService.getCartTotal();

        return ResponseEntity.ok(new CartSummary(itemCount, total));
    }

    // Inner class for cart summary
    static class CartSummary {
        private final int itemCount;
        private final double total;

        public CartSummary(int itemCount, double total) {
            this.itemCount = itemCount;
            this.total = total;
        }

        public int getItemCount() {
            return itemCount;
        }

        public double getTotal() {
            return total;
        }
    }
}