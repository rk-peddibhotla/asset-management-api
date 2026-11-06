package com.ravikanth.asset_management.organization;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationSettingsService {

    private static final Short SETTINGS_ID = 1;

    private final OrganizationSettingsRepository settingsRepository;

    public OrganizationSettingsService(
            OrganizationSettingsRepository settingsRepository
    ) {
        this.settingsRepository = settingsRepository;
    }

    @Transactional(readOnly = true)
    public OrganizationSettingsResponse getSettings() {
        OrganizationSettings settings = getSettingsEntity();

        return OrganizationSettingsResponse.from(settings);
    }

    @Transactional
    public OrganizationSettingsResponse updateSettings(
            UpdateOrganizationSettingsRequest request
    ) {
        OrganizationSettings settings = getSettingsEntity();

        settings.setOrganizationName(request.organizationName());
        settings.setResourceLabel(request.resourceLabel());
        settings.setMaxActiveBorrowings(request.maxActiveBorrowings());
        settings.setBorrowingDurationDays(request.borrowingDurationDays());
        settings.setDailyLateFee(request.dailyLateFee());
        settings.setBorrowingApprovalRequired(
                request.borrowingApprovalRequired()
        );
        settings.setAccountApprovalRequired(
                request.accountApprovalRequired()
        );
        settings.setEmailNotificationsEnabled(
                request.emailNotificationsEnabled()
        );

        OrganizationSettings updatedSettings =
                settingsRepository.save(settings);

        return OrganizationSettingsResponse.from(updatedSettings);
    }

    private OrganizationSettings getSettingsEntity() {
        return settingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "Organization settings have not been initialized"
                ));
    }
}