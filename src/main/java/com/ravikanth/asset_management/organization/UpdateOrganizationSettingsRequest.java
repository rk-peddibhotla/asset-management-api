package com.ravikanth.asset_management.organization;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateOrganizationSettingsRequest(

        @NotBlank(message = "Organization name is required")
        @Size(max = 150, message = "Organization name must not exceed 150 characters")
        String organizationName,

        @NotBlank(message = "Resource label is required")
        @Size(max = 50, message = "Resource label must not exceed 50 characters")
        String resourceLabel,

        @NotNull(message = "Maximum active borrowings is required")
        @Min(value = 1, message = "Maximum active borrowings must be at least 1")
        Integer maxActiveBorrowings,

        @NotNull(message = "Borrowing duration is required")
        @Min(value = 1, message = "Borrowing duration must be at least 1 day")
        Integer borrowingDurationDays,

        @NotNull(message = "Daily late fee is required")
        @DecimalMin(value = "0.00", message = "Daily late fee cannot be negative")
        BigDecimal dailyLateFee,

        @NotNull(message = "Borrowing approval setting is required")
        Boolean borrowingApprovalRequired,

        @NotNull(message = "Account approval setting is required")
        Boolean accountApprovalRequired,

        @NotNull(message = "Email notification setting is required")
        Boolean emailNotificationsEnabled
) {
}