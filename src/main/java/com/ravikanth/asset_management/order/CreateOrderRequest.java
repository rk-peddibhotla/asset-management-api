
package com.ravikanth.asset_management.order;

import jakarta.validation.constraints.NotNull;

// Data received from the frontend when creating an order
public class CreateOrderRequest {

    // Asset ID is required
    @NotNull(message = "Asset ID is required")
    private Long assetId;

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }
}

