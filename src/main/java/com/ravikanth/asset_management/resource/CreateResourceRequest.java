package com.ravikanth.asset_management.resource;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Data received from the frontend when creating a resource
public class CreateResourceRequest {

    // Resource name is required and cannot be longer than 200 characters
    @NotBlank(message = "Resource name is required")
    @Size(max = 200, message = "Resource name must not exceed 200 characters")
    private String name;

    // Manufacturer is optional, but has a maximum length
    @Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    private String manufacturer;

    // Purchase price is required and cannot be negative
    @NotNull(message = "Purchase price is required")
    @DecimalMin(
            value = "0.00",
            message = "Purchase price must not be negative"
    )
    private BigDecimal purchasePrice;

    // Description is optional
    private String description;

    // Category ID is required
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}