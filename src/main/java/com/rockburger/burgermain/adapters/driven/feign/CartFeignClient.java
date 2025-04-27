package com.rockburger.burgermain.adapters.driven.feign;

import com.rockburger.burgermain.adapters.driven.feign.dto.AddCartItemRequest;
import com.rockburger.burgermain.adapters.driven.feign.dto.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "cart-service",
        url = "${cart.service.url}"
)
public interface CartFeignClient {
    @GetMapping("/cart")
    ResponseEntity<CartResponse> getActiveCart(@RequestHeader("Authorization") String authHeader);

    @PostMapping("/cart/items")
    ResponseEntity<CartResponse> addItemToCart(@RequestHeader("Authorization") String authHeader, @RequestBody AddCartItemRequest request);

    @DeleteMapping("/cart/items/{articleId}")
    ResponseEntity<CartResponse> removeItemFromCart(@RequestHeader("Authorization") String authHeader, @PathVariable Long articleId);

    @DeleteMapping("/cart")
    ResponseEntity<Void> clearCart(@RequestHeader("Authorization") String authHeader);

    @GetMapping("/actuator/health")
    ResponseEntity<String> healthCheck();
}