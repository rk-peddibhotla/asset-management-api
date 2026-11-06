package com.ravikanth.asset_management.resource;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/resource-categories")
public class ResourceCategoryController {

    private final ResourceCategoryService categoryService;

    public ResourceCategoryController(
            ResourceCategoryService categoryService
    ) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceCategoryResponse createCategory(
            @Valid @RequestBody CreateResourceCategoryRequest request
    ) {

        ResourceCategory category =
                categoryService.createCategory(
                        request.getCategory(),
                        request.getSubCategory()
                );

        return ResourceCategoryResponse.from(category);
    }

    @GetMapping
    public List<ResourceCategoryResponse> getAllCategories() {

        List<ResourceCategory> categories =
                categoryService.getAllCategories();

        List<ResourceCategoryResponse> responses = new ArrayList<>();

        for (ResourceCategory category : categories) {
            responses.add(ResourceCategoryResponse.from(category));
        }

        return responses;
    }
}