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
    ResponseEntity<CartResponse> getActiveCart();

    @PostMapping("/cart/items")
    ResponseEntity<CartResponse> addItemToCart(@RequestBody AddCartItemRequest request);

    @DeleteMapping("/cart/items/{articleId}")
    ResponseEntity<CartResponse> removeItemFromCart(@PathVariable Long articleId);

    @DeleteMapping("/cart")
    ResponseEntity<Void> clearCart();

    @GetMapping("/actuator/health")
    ResponseEntity<String> healthCheck();
}