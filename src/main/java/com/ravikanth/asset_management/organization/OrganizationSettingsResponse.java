package com.ravikanth.asset_management.organization;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrganizationSettingsResponse(
        String organizationName,
        String resourceLabel,
        Integer maxActiveBorrowings,
        Integer borrowingDurationDays,
        BigDecimal dailyLateFee,
        boolean borrowingApprovalRequired,
        boolean accountApprovalRequired,
        boolean emailNotificationsEnabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static OrganizationSettingsResponse from(
            OrganizationSettings settings
    ) {
        return new OrganizationSettingsResponse(
                settings.getOrganizationName(),
                settings.getResourceLabel(),
                settings.getMaxActiveBorrowings(),
                settings.getBorrowingDurationDays(),
                settings.getDailyLateFee(),
                settings.isBorrowingApprovalRequired(),
                settings.isAccountApprovalRequired(),
                settings.isEmailNotificationsEnabled(),
                settings.getCreatedAt(),
                settings.getUpdatedAt()
        );
    }
}