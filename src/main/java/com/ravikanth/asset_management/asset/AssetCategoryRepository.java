package com.ravikanth.asset_management.asset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetCategoryRepository
        extends JpaRepository<AssetCategory, Long> {

    // Find a category using both category and sub-category
    Optional<AssetCategory> findByCategoryAndSubCategory(
            String category,
            String subCategory
    );
}

