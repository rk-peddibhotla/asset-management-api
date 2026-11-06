package com.ravikanth.asset_management.resource;

import java.math.BigDecimal;

// Data returned to the frontend for a resource
public class ResourceResponse {

    private Long id;
    private String name;
    private String manufacturer;
    private BigDecimal purchasePrice;
    private String description;
    private ResourceStatus status;
    private Long categoryId;
    private String category;
    private String subCategory;

    // Create an API response using the given values
    public ResourceResponse(
            Long id,
            String name,
            String manufacturer,
            BigDecimal purchasePrice,
            String description,
            ResourceStatus status,
            Long categoryId,
            String category,
            String subCategory
    ) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.purchasePrice = purchasePrice;
        this.description = description;
        this.status = status;
        this.categoryId = categoryId;
        this.category = category;
        this.subCategory = subCategory;
    }

    // Convert a Resource entity into a ResourceResponse
    public static ResourceResponse from(Resource resource) {

        ResourceCategory category = resource.getCategory();

        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getManufacturer(),
                resource.getPurchasePrice(),
                resource.getDescription(),
                resource.getStatus(),
                category.getId(),
                category.getCategory(),
                category.getSubCategory()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public String getDescription() {
        return description;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
    }
}