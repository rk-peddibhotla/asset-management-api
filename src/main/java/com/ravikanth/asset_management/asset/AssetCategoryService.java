
package com.ravikanth.asset_management.asset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AssetCategoryService {

    private final AssetCategoryRepository categoryRepository;

    public AssetCategoryService(
            AssetCategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    // Create a new asset category
    @Transactional
    public AssetCategory createCategory(
            String category,
            String subCategory
    ) {

        // Check whether this category and sub-category already exist
        Optional<AssetCategory> existingCategory =
                categoryRepository.findByCategoryAndSubCategory(
                        category,
                        subCategory
                );

        if (existingCategory.isPresent()) {
            throw new IllegalArgumentException(
                    "Asset category already exists"
            );
        }

        // Create a new category
        AssetCategory assetCategory = new AssetCategory();

        assetCategory.setCategory(category);
        assetCategory.setSubCategory(subCategory);

        // Save the category in the database
        return categoryRepository.save(assetCategory);
    }

    // Get all asset categories
    @Transactional(readOnly = true)
    public List<AssetCategory> getAllCategories() {
        return categoryRepository.findAll();
    }
}

