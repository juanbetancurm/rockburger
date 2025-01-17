package com.rockburger.arquetipo2024.adapters.driving.http.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddArticleRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be less than or equal to 50 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 90, message = "Description must be less than or equal to 90 characters")
    private String description;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private double price;

    //@Size(min = 1, max = 3, message = "Article must have between 1 and 3 categories")
    @JsonProperty("categoryIds")
    private Set<Long> categoryIds;

    @NotNull(message = "Brand ID is required")
    @JsonProperty("brandId")
    private Long brandId;

}

