package com.ravikanth.asset_management.resource;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResourceCategoryRepository
        extends JpaRepository<ResourceCategory, Long> {

    Optional<ResourceCategory> findByCategoryAndSubCategory(
            String category,
            String subCategory
    );
}