package com.rockburger.burgermain.adapters.driving.http.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BrandResponse {
    private Long id;
    private String name;
    private String description;
}

