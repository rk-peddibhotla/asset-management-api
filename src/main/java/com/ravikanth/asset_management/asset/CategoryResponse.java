
package com.ravikanth.asset_management.asset;

// Data returned to the frontend for an asset category
public class CategoryResponse {

    private Long id;
    private String category;
    private String subCategory;

    // Create an API response using the given values
    public CategoryResponse(
            Long id,
            String category,
            String subCategory
    ) {
        this.id = id;
        this.category = category;
        this.subCategory = subCategory;
    }

    // Convert an AssetCategory entity into a CategoryResponse
    public static CategoryResponse from(
            AssetCategory assetCategory
    ) {
        return new CategoryResponse(
                assetCategory.getId(),
                assetCategory.getCategory(),
                assetCategory.getSubCategory()
        );
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
    }
}
