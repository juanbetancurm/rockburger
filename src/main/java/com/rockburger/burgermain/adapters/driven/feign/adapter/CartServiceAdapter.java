package com.rockburger.burgermain.adapters.driven.feign.adapter;

import com.rockburger.burgermain.adapters.driven.feign.CartFeignClient;
import com.rockburger.burgermain.adapters.driven.feign.dto.AddCartItemRequest;
import com.rockburger.burgermain.adapters.driven.feign.dto.CartResponse;
import com.rockburger.burgermain.adapters.driven.feign.exception.CartServiceExceptionTranslator;
import com.rockburger.burgermain.domain.exception.DuplicateCartItemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CartServiceAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CartServiceAdapter.class);

    private final CartFeignClient cartFeignClient;
    private final CartServiceExceptionTranslator exceptionTranslator;

    public CartServiceAdapter(CartFeignClient cartFeignClient,
                              CartServiceExceptionTranslator exceptionTranslator) {
        this.cartFeignClient = cartFeignClient;
        this.exceptionTranslator = exceptionTranslator;
    }

    public CartResponse getActiveCart() {
        logger.debug("Getting active cart");
        try {
            CartResponse response = cartFeignClient.getActiveCart().getBody();
            logger.debug("Received response from cart service: {}", response != null ? "success" : "null");
            return response != null ? response : getFallbackCart();
        } catch (Exception e) {
            logger.error("Error in getActiveCart: {}", e.getMessage(), e);
            return getFallbackCart();
        }
    }

    public CartResponse addItemToCart(Long articleId, String articleName, int quantity, double price) {
        logger.info("=== CART SERVICE ADAPTER DEBUG ===");
        logger.info("Adding item to cart: articleId={}, name={}, quantity={}, price={}", articleId, articleName, quantity, price);
        logger.info("Thread ID: {}", Thread.currentThread().getId());
        logger.info("Thread Name: {}", Thread.currentThread().getName());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Authentication exists before Feign call: {}", auth != null);
        if (auth != null) {
            logger.info("Auth credentials exists: {}", auth.getCredentials() != null);
            logger.info("Auth credentials is String: {}", auth.getCredentials() instanceof String);
            if (auth.getCredentials() instanceof String) {
                String token = (String) auth.getCredentials();
                logger.info("Token length: {}, starts with eyJ: {}", token.length(), token.startsWith("eyJ"));
            }
        }
        logger.info("=== END CART SERVICE ADAPTER DEBUG ===");

        try {
            AddCartItemRequest request = new AddCartItemRequest();
            request.setArticleId(articleId);
            request.setArticleName(articleName);
            request.setQuantity(quantity);
            request.setPrice(price);

            logger.info("Making Feign client call to cart service...");

            CartResponse response = cartFeignClient.addItemToCart(request).getBody();
            logger.info("Feign call completed successfully");
            return response != null ? response : getFallbackCart();

        } catch (feign.FeignException.Conflict e) {
            // Handle 409 - Item already exists in cart
            logger.warn("Item already exists in cart for articleId {}: {}", articleId, e.getMessage());

            // Return current cart state instead of fallback
            try {
                logger.info("Fetching current cart state since item already exists");
                return getActiveCart();
            } catch (Exception fallbackException) {
                logger.error("Failed to get current cart after duplicate detection: {}", fallbackException.getMessage());
                return getFallbackCart();
            }

        } catch (feign.FeignException.BadRequest e) {
            // Handle 400 - Validation errors
            logger.error("Validation error for articleId {}: {}", articleId, e.getMessage());
            throw new IllegalArgumentException("Invalid cart request: " + e.getMessage());

        } catch (feign.FeignException.NotFound e) {
            // Handle 404 - Cart or article not found
            logger.error("Resource not found for articleId {}: {}", articleId, e.getMessage());
            throw new RuntimeException("Cart or article not found: " + e.getMessage());

        } catch (feign.FeignException e) {
            // Handle other HTTP errors based on status code
            logger.error("HTTP error {} for articleId {}: {}", e.status(), articleId, e.getMessage());

            if (e.status() >= 500) {
                // Server errors - use fallback for resilience
                logger.warn("Server error detected, using fallback cart");
                return getFallbackCart();
            } else {
                // Client errors (4xx) - don't hide the real problem
                throw new RuntimeException("Cart service error: " + e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error in addItemToCart: {} - {}", e.getClass().getSimpleName(), e.getMessage());

            // Handle the exception appropriately
            handleCartServiceException(e, articleId);

            // If we reach here, it was a server error or technical issue - use fallback
            logger.warn("Using fallback cart due to technical error");
            return getFallbackCart();
        }
    }

    public CartResponse removeItemFromCart(Long articleId) {
        logger.info("Removing item from cart: articleId={}", articleId);
        try {
            CartResponse response = cartFeignClient.removeItemFromCart(articleId).getBody();
            return response != null ? response : getFallbackCart();
        } catch (Exception e) {
            logger.error("Error in removeItemFromCart: {}", e.getMessage(), e);
            return getFallbackCart();
        }
    }

    /**
     * Clears the cart after a successful checkout operation.
     * This method is designed to be resilient to failures (Error Recovery).
     * The order creation should not fail if cart clearing fails.
     *
     * @throws RuntimeException if cart clearing fails - caller should handle this gracefully
     */
    public void clearCart() {
        logger.info("Clearing cart after successful checkout");
        try {
            cartFeignClient.clearCart();
            logger.info("Cart cleared successfully");
        } catch (feign.FeignException e) {
            // Log the specific HTTP error for better debugging
            logger.error("Failed to clear cart - HTTP {} error: {}", e.status(), e.getMessage());
            throw new RuntimeException("Cart service error during clear operation: " + e.getMessage(), e);
        } catch (Exception e) {
            // Log any other technical errors
            logger.error("Technical error while clearing cart: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("Failed to clear cart: " + e.getMessage(), e);
        }
    }

    /**
     * Alternative method that returns a boolean indicating success/failure
     * This can be used if you prefer not to handle exceptions for cart clearing.
     */
    public boolean tryClearCart() {
        logger.info("Attempting to clear cart (non-throwing version)");
        try {
            cartFeignClient.clearCart();
            logger.info("Cart cleared successfully");
            return true;
        } catch (Exception e) {
            logger.error("Failed to clear cart: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    private CartResponse getFallbackCart() {
        logger.warn("Returning fallback empty cart");
        CartResponse fallbackCart = new CartResponse();
        fallbackCart.setItems(new ArrayList<>());
        fallbackCart.setTotal(0.0);
        fallbackCart.setStatus("FALLBACK");
        return fallbackCart;
    }

    private void handleCartServiceException(Exception e, Long articleId) {
        if (e instanceof feign.FeignException.Conflict) {
            // Throw a domain-specific exception that the controller can handle
            throw new DuplicateCartItemException("Item already exists in cart", articleId);
        } else if (e instanceof feign.FeignException.BadRequest) {
            throw new IllegalArgumentException("Invalid cart request: " + e.getMessage());
        } else if (e instanceof feign.FeignException.NotFound) {
            throw new RuntimeException("Cart or article not found: " + e.getMessage());
        } else if (e instanceof feign.FeignException) {
            feign.FeignException feignEx = (feign.FeignException) e;
            if (feignEx.status() >= 500) {
                // Server errors - don't throw, just log and continue with fallback
                logger.warn("Server error detected, will use fallback cart");
            } else {
                throw new RuntimeException("Cart service error: " + feignEx.getMessage());
            }
        } else {
            // Technical errors - don't throw, just log and continue with fallback
            logger.error("Technical error, will use fallback cart: {}", e.getMessage());
        }
    }
}