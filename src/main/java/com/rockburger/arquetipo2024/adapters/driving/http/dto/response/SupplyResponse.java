package com.rockburger.arquetipo2024.adapters.driving.http.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplyResponse {
    private Long id;
    private Long articleId;
    private String articleName;
    private int quantity;
    private LocalDateTime supplyDate;
    private String supplierEmail;
}