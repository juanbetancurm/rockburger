package com.rockburger.arquetipo2024.adapters.driven.feign.adapter;

import com.rockburger.arquetipo2024.adapters.driven.feign.CartFeignClient;
import com.rockburger.arquetipo2024.adapters.driven.feign.dto.CartResponse;
import com.rockburger.arquetipo2024.adapters.driven.feign.dto.AddCartItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceAdapter {
    private final CartFeignClient cartFeignClient;

    public CartResponse getActiveCart(String authToken) {
        return cartFeignClient.getActiveCart("Bearer " + authToken).getBody();
    }

    public CartResponse addItemToCart(String authToken, Long articleId, String articleName, int quantity, double price) {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setArticleId(articleId);
        request.setArticleName(articleName);
        request.setQuantity(quantity);
        request.setPrice(price);

        return cartFeignClient.addItemToCart("Bearer " + authToken, request).getBody();
    }

    public CartResponse removeItemFromCart(String authToken, Long articleId) {
        return cartFeignClient.removeItemFromCart("Bearer " + authToken, articleId).getBody();
    }

    public void clearCart(String authToken) {
        cartFeignClient.clearCart("Bearer " + authToken);
    }
}
