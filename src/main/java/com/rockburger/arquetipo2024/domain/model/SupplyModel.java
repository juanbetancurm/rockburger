package com.rockburger.arquetipo2024.domain.model;

import com.rockburger.arquetipo2024.domain.exception.InvalidSupplyException;

import java.time.LocalDateTime;

public class SupplyModel {
    private Long id;
    private ArticleModel article;
    private int quantity;
    private LocalDateTime supplyDate;
    private UserModel supplier;
    private Long version;

    public SupplyModel() {
        this.supplyDate = LocalDateTime.now();
    }

    public SupplyModel(ArticleModel article, int quantity, UserModel supplier) {
        this();
        this.article = article;
        this.quantity = quantity;
        this.supplier = supplier;
    }

    // Validation logic in the domain model
    public void validateSupply() {
        if (article == null) {
            throw new InvalidSupplyException("Article cannot be null");
        }
        if (quantity <= 0) {
            throw new InvalidSupplyException("Supply quantity must be greater than zero");
        }
        if (supplier == null) {
            throw new InvalidSupplyException("Supplier cannot be null");
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ArticleModel getArticle() {
        return article;
    }

    public void setArticle(ArticleModel article) {
        this.article = article;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getSupplyDate() {
        return supplyDate;
    }

    public void setSupplyDate(LocalDateTime supplyDate) {
        this.supplyDate = supplyDate;
    }

    public UserModel getSupplier() {
        return supplier;
    }

    public void setSupplier(UserModel supplier) {
        this.supplier = supplier;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}