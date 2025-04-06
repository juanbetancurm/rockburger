package com.rockburger.arquetipo2024.adapters.driven.feign;

import com.rockburger.arquetipo2024.adapters.driven.feign.dto.AddCartItemRequest;
import com.rockburger.arquetipo2024.adapters.driven.feign.dto.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cart-service", url = "${cart.service.url}")
public interface CartFeignClient {

    @GetMapping("/cart")
    ResponseEntity<CartResponse> getActiveCart(@RequestHeader("Authorization") String authToken);

    @PostMapping("/cart/items")
    ResponseEntity<CartResponse> addItemToCart(
            @RequestHeader("Authorization") String authToken,
            @RequestBody AddCartItemRequest request
    );

    @DeleteMapping("/cart/items/{articleId}")
    ResponseEntity<CartResponse> removeItemFromCart(
            @RequestHeader("Authorization") String authToken,
            @PathVariable Long articleId
    );

    @DeleteMapping("/cart")
    ResponseEntity<Void> clearCart(@RequestHeader("Authorization") String authToken);
}