package com.rockburger.arquetipo2024.adapters.driven.feign.dto;

import lombok.Data;

@Data
public class CartItemResponse {
    private Long id;
    private Long articleId;
    private String articleName;
    private int quantity;
    private double price;
    private double subtotal;
}
