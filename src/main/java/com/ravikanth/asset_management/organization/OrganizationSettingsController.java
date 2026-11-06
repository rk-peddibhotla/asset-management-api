package com.ravikanth.asset_management.organization;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organization-settings")
public class OrganizationSettingsController {

    private final OrganizationSettingsService settingsService;

    public OrganizationSettingsController(
            OrganizationSettingsService settingsService
    ) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public OrganizationSettingsResponse getSettings() {
        return settingsService.getSettings();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizationSettingsResponse updateSettings(
            @Valid @RequestBody UpdateOrganizationSettingsRequest request
    ) {
        return settingsService.updateSettings(request);
    }
}