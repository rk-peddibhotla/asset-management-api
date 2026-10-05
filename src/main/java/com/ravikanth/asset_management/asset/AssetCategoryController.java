
package com.ravikanth.asset_management.asset;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class AssetCategoryController {

    private final AssetCategoryService categoryService;

    public AssetCategoryController(
            AssetCategoryService categoryService
    ) {
        this.categoryService = categoryService;
    }

    // Create a new asset category
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {

        AssetCategory category =
                categoryService.createCategory(
                        request.getCategory(),
                        request.getSubCategory()
                );

        return CategoryResponse.from(category);
    }

    // Get all asset categories
    @GetMapping
    public List<CategoryResponse> getAllCategories() {

        List<AssetCategory> categories =
                categoryService.getAllCategories();

        List<CategoryResponse> responses = new ArrayList<>();

        for (AssetCategory category : categories) {
            CategoryResponse response =
                    CategoryResponse.from(category);

            responses.add(response);
        }

        return responses;
    }
}
