
package com.ravikanth.asset_management.asset;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    // Create a new asset
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse createAsset(
            @Valid @RequestBody CreateAssetRequest request
    ) {

        Asset asset = assetService.createAsset(
                request.getName(),
                request.getManufacturer(),
                request.getPrice(),
                request.getCategoryId()
        );

        return AssetResponse.from(asset);
    }

    // Get all assets
    @GetMapping
    public List<AssetResponse> getAllAssets() {

        List<Asset> assets = assetService.getAllAssets();
        List<AssetResponse> responses = new ArrayList<>();

        for (Asset asset : assets) {
            AssetResponse response = AssetResponse.from(asset);
            responses.add(response);
        }

        return responses;
    }
}
