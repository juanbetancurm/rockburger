package com.rockburger.arquetipo2024.domain.model;



import com.rockburger.arquetipo2024.domain.exception.BlankFieldException;
import com.rockburger.arquetipo2024.domain.exception.DuplicateCategoryException;
import com.rockburger.arquetipo2024.domain.exception.InvalidCategoryCountException;

import com.rockburger.arquetipo2024.domain.exception.InvalidParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ArticleModel {
    private static final Logger logger = LoggerFactory.getLogger(ArticleModel.class);
    private Long id;
    private String name;
    private String description;
    private int quantity;
    private double price;
    private Set<CategoryModel> categories = new HashSet<>();
    private BrandModel brand;
    private Long brandId;
    private String brandName;

    public String getBrandName() {
        if (brand != null) {
            return brand.getName();
        }
        return null;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public ArticleModel() {
    }

    public ArticleModel(Long id, String name, String description, int quantity, double price, Set<CategoryModel> categories, Long brandId) {

        logger.info("Received categories: {}", categories);
        this.id = id;
        this.name = name.trim();
        this.description = description.trim();
        this.quantity = quantity;
        this.price = price;
        this.categories = categories;
        this.brandId = brandId;
        logger.info("Received brand: {}", brandId);
        logger.info("Received categories: {}", categories);
    }

   public Set<Long> getCategoryIds() {
        if (categories == null) {
            return Collections.emptySet();
        }
        return categories.stream()
                .map(CategoryModel::getId)
                .collect(Collectors.toSet());
    }
    @Override
    public String toString() {
        return "ArticleModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", categories=" + categories +
                ", brand=" + brand +
                ", brandName=" + brandName +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BlankFieldException("Name cannot be blank");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {

        if (description == null || description.trim().isEmpty()) {
            throw new BlankFieldException("Description cannot be blank");
        }
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidParameterException("Article must have a valid quantity greater than 0");
        }

        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price <= 0) {
            throw new InvalidParameterException("Article must have a valid price greater than 0");
        }
        this.price = price;
    }

    public Set<CategoryModel> getCategories() {
        return categories;
    }

    public void setCategories(Set<CategoryModel> categories) {

        if (categories == null || categories.isEmpty() || categories.size() > 3) {
            throw new InvalidCategoryCountException("Article must have between 1 and 3 categories");
        }
        Set<Long> uniqueCategoryIds = new HashSet<>();
        for (CategoryModel category : categories) {
            if (!uniqueCategoryIds.add(category.getId())) {
                throw new DuplicateCategoryException("Article cannot have duplicate categories");
            }
        }
        logger.info("Categories after duplicate check: {}", categories);

        this.categories = categories;
    }

    public BrandModel getBrand() {
        return brand;
    }
    public void setBrand(BrandModel brand) {
        this.brand = brand;
    }
    public Long getBrandId() {
        if (brand != null) {
            return brand.getId();
        }
        return null;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }
}
