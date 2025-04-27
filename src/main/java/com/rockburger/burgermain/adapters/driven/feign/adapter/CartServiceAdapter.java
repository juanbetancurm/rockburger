package com.rockburger.burgermain.adapters.driven.feign.adapter;

import com.rockburger.burgermain.adapters.driven.feign.CartFeignClient;
import com.rockburger.burgermain.adapters.driven.feign.dto.AddCartItemRequest;
import com.rockburger.burgermain.adapters.driven.feign.dto.CartResponse;
import com.rockburger.burgermain.adapters.driven.feign.exception.CartServiceExceptionTranslator;
import com.rockburger.burgermain.configuration.security.JwtContextHolder;
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
    private final JwtContextHolder jwtContextHolder;

    public CartServiceAdapter(CartFeignClient cartFeignClient,
                              CartServiceExceptionTranslator exceptionTranslator,
                              JwtContextHolder jwtContextHolder) {
        this.cartFeignClient = cartFeignClient;
        this.exceptionTranslator = exceptionTranslator;
        this.jwtContextHolder = jwtContextHolder;
    }

    // Improved token extraction method
    private String getAuthHeaderFromSecurityContext() {
        // Try getting token from thread-local storage first
        String token = jwtContextHolder.getToken();

        // If not found, try from security context
        if (token == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getCredentials() instanceof String) {
                token = (String) auth.getCredentials();
                // Store for future use
                jwtContextHolder.setToken(token);
                logger.debug("Token extracted from security context and stored in context holder");
            }
        }

        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }

        logger.warn("No authentication token found in security context");
        // Better error handling - return null instead of empty token
        return null;
    }

    public CartResponse getActiveCart() {
        logger.debug("Getting active cart");
        try {
            String authHeader = getAuthHeaderFromSecurityContext();
            if (authHeader == null) {
                logger.error("Authentication token required but not available");
                return getFallbackCart();
            }
            logger.debug("Sending request to cart service with auth header");
            CartResponse response = cartFeignClient.getActiveCart(authHeader).getBody();
            logger.debug("Received response from cart service: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Error in getActiveCart: {}", e.getMessage(), e);
            return getFallbackCart();
        }
    }

    public CartResponse addItemToCart(Long articleId, String articleName, int quantity, double price) {
        logger.info("Adding item to cart: articleId={}, name={}, quantity={}, price={}",
                articleId, articleName, quantity, price);

        try {
            String authHeader = getAuthHeaderFromSecurityContext();
            if (authHeader == null) {
                logger.error("Authentication token required but not available");
                return getFallbackCart();
            }

            AddCartItemRequest request = new AddCartItemRequest();
            request.setArticleId(articleId);
            request.setArticleName(articleName);
            request.setQuantity(quantity);
            request.setPrice(price);

            logger.debug("Sending addItemToCart request to cart service: {}", request);
            CartResponse response = cartFeignClient.addItemToCart(authHeader, request).getBody();
            logger.debug("Received response from cart service: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("Error in addItemToCart: {}", e.getMessage(), e);
            return getFallbackCart();
        }
    }

    public CartResponse removeItemFromCart(Long articleId) {
        logger.info("Removing item from cart: articleId={}", articleId);

        try {
            String authHeader = getAuthHeaderFromSecurityContext();
            if (authHeader == null) {
                logger.error("Authentication token required but not available");
                return getFallbackCart();
            }
            return cartFeignClient.removeItemFromCart(authHeader, articleId).getBody();
        } catch (Exception e) {
            logger.error("Error in removeItemFromCart: {}", e.getMessage(), e);
            return getFallbackCart();
        }
    }

    public void clearCart() {
        logger.info("Clearing cart");
        try {
            String authHeader = getAuthHeaderFromSecurityContext();
            if (authHeader == null) {
                logger.error("Authentication token required but not available");
                return;
            }
            cartFeignClient.clearCart(authHeader);
        } catch (Exception e) {
            logger.error("Error in clearCart: {}", e.getMessage(), e);
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
}