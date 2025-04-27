package com.rockburger.burgermain.adapters.driven.feign.facade;

import com.rockburger.burgermain.adapters.driven.feign.adapter.CartServiceAdapter;
import com.rockburger.burgermain.adapters.driven.feign.dto.CartResponse;
import com.rockburger.burgermain.domain.model.ArticleModel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Facade service that wraps the cart service adapter and provides
 * a simpler interface for the application code to interact with the cart service.
 */
@Service
@RequiredArgsConstructor
public class CartFacadeService {
    private static final Logger logger = LoggerFactory.getLogger(CartFacadeService.class);

    private final CartServiceAdapter cartServiceAdapter;

    /**
     * Get the active cart for the current user
     */
    public CartResponse getActiveCart() {
        logger.debug("Getting active cart");
        return cartServiceAdapter.getActiveCart();
    }

    /**
     * Add an article to the cart
     */
    public CartResponse addArticleToCart(ArticleModel article, int quantity) {
        logger.info("Adding article to cart: {}, quantity: {}", article.getName(), quantity);

        return cartServiceAdapter.addItemToCart(
                article.getId(),
                article.getName(),
                quantity,
                article.getPrice()
        );
    }

    /**
     * Remove an article from the cart
     */
    public CartResponse removeArticleFromCart(Long articleId) {
        logger.info("Removing article from cart: {}", articleId);
        return cartServiceAdapter.removeItemFromCart(articleId);
    }

    /**
     * Clear the entire cart
     */
    public void clearCart() {
        logger.info("Clearing cart");
        cartServiceAdapter.clearCart();
    }

    /**
     * Get cart total price
     */
    public double getCartTotal() {
        CartResponse cart = cartServiceAdapter.getActiveCart();
        return cart.getTotal();
    }

    /**
     * Get cart item count
     */
    public int getCartItemCount() {
        CartResponse cart = cartServiceAdapter.getActiveCart();
        return cart.getItems() != null ? cart.getItems().size() : 0;
    }
}