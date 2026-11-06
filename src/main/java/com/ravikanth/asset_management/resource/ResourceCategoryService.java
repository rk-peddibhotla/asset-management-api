package com.ravikanth.asset_management.resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceCategoryService {

    private final ResourceCategoryRepository categoryRepository;

    public ResourceCategoryService(
            ResourceCategoryRepository categoryRepository
    ) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ResourceCategory createCategory(
            String category,
            String subCategory
    ) {

        // Check whether this category and sub-category already exist
        Optional<ResourceCategory> existingCategory =
                categoryRepository.findByCategoryAndSubCategory(
                        category,
                        subCategory
                );

        if (existingCategory.isPresent()) {
            throw new IllegalArgumentException(
                    "Resource category already exists"
            );
        }

        // Create a new resource category
        ResourceCategory resourceCategory = new ResourceCategory();

        resourceCategory.setCategory(category);
        resourceCategory.setSubCategory(subCategory);

        // Save the category in the database
        return categoryRepository.save(resourceCategory);
    }

    @Transactional(readOnly = true)
    public List<ResourceCategory> getAllCategories() {
        return categoryRepository.findAll();
    }
}