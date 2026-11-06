package com.ravikanth.asset_management.resource;

import jakarta.validation.constraints.NotNull;

// Data received when updating a resource's availability
public record UpdateResourceAvailabilityRequest(

        @NotNull(message = "Resource status is required")
        ResourceStatus status
) {
}