package com.rockburger.arquetipo2024.adapters.driven.feign.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartResponse {
    private Long id;
    private String userId;
    private List<CartItemResponse> items;
    private double total;
    private LocalDateTime lastUpdated;
    private String status;
}