package com.rockburger.arquetipo2024.adapters.driving.http.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleResponse {
    private Long id;
    private String name;
    private String description;
    private int quantity;
    private double price;
    private Set<CategoryResponse> categories;
    private Long brandId;
    private String brandName;
    private BrandResponse brand;
}

