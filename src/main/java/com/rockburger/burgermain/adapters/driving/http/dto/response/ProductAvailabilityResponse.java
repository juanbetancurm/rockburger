package com.rockburger.burgermain.adapters.driving.http.dto.response;

public class ProductAvailabilityResponse {
    private Long id;
    private String name;
    private int availableQuantity;
    private double price;

    public ProductAvailabilityResponse() {}

    public ProductAvailabilityResponse(Long id, String name, int availableQuantity, double price) {
        this.id = id;
        this.name = name;
        this.availableQuantity = availableQuantity;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
