package com.ravikanth.asset_management.borrowing;

import jakarta.validation.constraints.NotNull;

// Data received from the frontend when creating a borrowing request
public class CreateBorrowingRequest {

    // Resource ID is required
    @NotNull(message = "Resource ID is required")
    private Long resourceId;

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}