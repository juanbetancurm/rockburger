package com.rockburger.burgermain.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderModel {
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private String status;
    private List<OrderItemModel> items;

    public OrderModel() {
        this.orderDate = LocalDateTime.now();
        this.status = "COMPLETED";
    }

    public OrderModel(Long userId, BigDecimal totalAmount, List<OrderItemModel> items) {
        this();
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderItemModel> getItems() { return items; }
    public void setItems(List<OrderItemModel> items) { this.items = items; }
}