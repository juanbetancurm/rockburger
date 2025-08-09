package com.rockburger.burgermain.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalesSummaryModel {
    private LocalDate date;
    private int totalOrders;
    private BigDecimal totalRevenue;

    public SalesSummaryModel() {}

    public SalesSummaryModel(LocalDate date, int totalOrders, BigDecimal totalRevenue) {
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