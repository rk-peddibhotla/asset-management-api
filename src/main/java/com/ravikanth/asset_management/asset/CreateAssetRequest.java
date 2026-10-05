
package com.ravikanth.asset_management.asset;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Data received from the frontend when creating an asset
public class CreateAssetRequest {

    // Asset name is required and cannot be longer than 200 characters
    @NotBlank(message = "Asset name is required")
    @Size(max = 200, message = "Asset name must not exceed 200 characters")
    private String name;

    // Manufacturer is optional, but has a maximum length
    @Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    private String manufacturer;

    // Price is required and cannot be negative
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must not be negative")
    private BigDecimal price;

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}

