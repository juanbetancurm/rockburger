package com.rockburger.burgermain.adapters.driving.http.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalesSummaryResponse {
    private LocalDate date;
    private int totalOrders;
    private BigDecimal totalRevenue;

    public SalesSummaryResponse() {}

    public SalesSummaryResponse(LocalDate date, int totalOrders, BigDecimal totalRevenue) {
        this.date = date;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
}
