package com.ravikanth.asset_management.resource;

public class ResourceCategoryResponse {

    private Long id;
    private String category;
    private String subCategory;

    public ResourceCategoryResponse(
            Long id,
            String category,
            String subCategory
    ) {
        this.id = id;
        this.category = category;
        this.subCategory = subCategory;
    }

    public static ResourceCategoryResponse from(
            ResourceCategory resourceCategory
    ) {
        return new ResourceCategoryResponse(
                resourceCategory.getId(),
                resourceCategory.getCategory(),
                resourceCategory.getSubCategory()
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