package com.ravikanth.asset_management.asset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository categoryRepository;

    public AssetService(
            AssetRepository assetRepository,
            AssetCategoryRepository categoryRepository
    ) {
        this.assetRepository = assetRepository;
        this.categoryRepository = categoryRepository;
    }


    // Create a new asset
    @Transactional
    public Asset createAsset(
            String name,
            String manufacturer,
            BigDecimal price,
            Long categoryId
    ) {

        // Find the category using the category ID
        Optional<AssetCategory> categoryResult =
                categoryRepository.findById(categoryId);

        // Make sure the category exists
        if (categoryResult.isEmpty()) {
            throw new IllegalArgumentException(
                    "Asset category not found"
            );
        }

        AssetCategory category = categoryResult.get();

        // Create a new asset
        Asset asset = new Asset();

        asset.setName(name);
        asset.setManufacturer(manufacturer);
        asset.setPrice(price);
        asset.setCategory(category);

        // Save the asset in the database
        return assetRepository.save(asset);
    }


    // Get all assets
    @Transactional(readOnly = true)
    public List<Asset> getAllAssets() {

        return assetRepository.findAllWithCategory();
    }
}

