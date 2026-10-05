
package com.ravikanth.asset_management.asset;

import java.math.BigDecimal;

// Data returned to the frontend for an asset
public class AssetResponse {

    private Long id;
    private String name;
    private String manufacturer;
    private BigDecimal price;
    private Long categoryId;
    private String category;
    private String subCategory;

    // Create an API response using the given values
    public AssetResponse(
            Long id,
            String name,
            String manufacturer,
            BigDecimal price,
            Long categoryId,
            String category,
            String subCategory
    ) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.price = price;
        this.categoryId = categoryId;
        this.category = category;
        this.subCategory = subCategory;
    }

    // Convert an Asset entity into an AssetResponse
    public static AssetResponse from(Asset asset) {

        AssetCategory category = asset.getCategory();

        return new AssetResponse(
                asset.getId(),
                asset.getName(),
                asset.getManufacturer(),
                asset.getPrice(),
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

    public BigDecimal getPrice() {
        return price;
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
