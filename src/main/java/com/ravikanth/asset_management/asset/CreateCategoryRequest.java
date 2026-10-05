
package com.ravikanth.asset_management.asset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Data received from the frontend when creating an asset category
public class CreateCategoryRequest {

    // Category name is required and cannot be longer than 100 characters
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    // Sub-category name is required and cannot be longer than 100 characters
    @NotBlank(message = "Sub-category is required")
    @Size(max = 100, message = "Sub-category must not exceed 100 characters")
    private String subCategory;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }
}

