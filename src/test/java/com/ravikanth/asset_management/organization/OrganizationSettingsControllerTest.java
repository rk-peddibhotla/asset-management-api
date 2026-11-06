
package com.ravikanth.asset_management.organization;

import com.ravikanth.asset_management.user.AccountStatus;
import com.ravikanth.asset_management.user.User;
import com.ravikanth.asset_management.user.UserRepository;
import com.ravikanth.asset_management.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrganizationSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationSettingsRepository settingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================================================
    // GET ORGANIZATION SETTINGS
    // =========================================================

    @Test
    void authenticatedUser_shouldGetOrganizationSettings()
            throws Exception {

        User user = createUser(UserRole.USER);

        mockMvc.perform(
                get("/api/organization-settings")
                        .with(userJwt(user))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.organizationName").isNotEmpty())
        .andExpect(jsonPath("$.resourceLabel").isNotEmpty())
        .andExpect(jsonPath("$.maxActiveBorrowings").isNumber())
        .andExpect(jsonPath("$.borrowingDurationDays").isNumber())
        .andExpect(jsonPath("$.dailyLateFee").isNumber())
        .andExpect(jsonPath("$.borrowingApprovalRequired").isBoolean())
        .andExpect(jsonPath("$.accountApprovalRequired").isBoolean())
        .andExpect(jsonPath("$.emailNotificationsEnabled").isBoolean());
    }

    @Test
    void unauthenticatedUser_shouldNotGetOrganizationSettings()
            throws Exception {

        mockMvc.perform(
                get("/api/organization-settings")
        )
        .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // UPDATE ORGANIZATION SETTINGS
    // =========================================================

    @Test
    void admin_shouldUpdateOrganizationSettings()
            throws Exception {

        User admin = createUser(UserRole.ADMIN);

        OrganizationSettings original = getSettings();

        String originalOrganizationName =
                original.getOrganizationName();

        String originalResourceLabel =
                original.getResourceLabel();

        Integer originalMaxActiveBorrowings =
                original.getMaxActiveBorrowings();

        Integer originalBorrowingDurationDays =
                original.getBorrowingDurationDays();

        BigDecimal originalDailyLateFee =
                original.getDailyLateFee();

        boolean originalBorrowingApprovalRequired =
                original.isBorrowingApprovalRequired();

        boolean originalAccountApprovalRequired =
                original.isAccountApprovalRequired();

        boolean originalEmailNotificationsEnabled =
                original.isEmailNotificationsEnabled();

        try {
            mockMvc.perform(
                    put("/api/organization-settings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "organizationName": "Test Organization",
                                        "resourceLabel": "Equipment",
                                        "maxActiveBorrowings": 5,
                                        "borrowingDurationDays": 14,
                                        "dailyLateFee": 75.00,
                                        "borrowingApprovalRequired": false,
                                        "accountApprovalRequired": false,
                                        "emailNotificationsEnabled": true
                                    }
                                    """)
                            .with(adminJwt(admin))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.organizationName")
                    .value("Test Organization"))
            .andExpect(jsonPath("$.resourceLabel")
                    .value("Equipment"))
            .andExpect(jsonPath("$.maxActiveBorrowings").value(5))
            .andExpect(jsonPath("$.borrowingDurationDays").value(14))
            .andExpect(jsonPath("$.dailyLateFee").value(75.00))
            .andExpect(jsonPath("$.borrowingApprovalRequired").value(false))
            .andExpect(jsonPath("$.accountApprovalRequired").value(false))
            .andExpect(jsonPath("$.emailNotificationsEnabled").value(true));

            OrganizationSettings updated = getSettings();

            assertEquals(
                    "Test Organization",
                    updated.getOrganizationName()
            );

            assertEquals(
                    "Equipment",
                    updated.getResourceLabel()
            );

            assertEquals(
                    5,
                    updated.getMaxActiveBorrowings()
            );

            assertEquals(
                    14,
                    updated.getBorrowingDurationDays()
            );

            assertEquals(
                    0,
                    updated.getDailyLateFee()
                            .compareTo(new BigDecimal("75.00"))
            );

        } finally {
            restoreSettings(
                    originalOrganizationName,
                    originalResourceLabel,
                    originalMaxActiveBorrowings,
                    originalBorrowingDurationDays,
                    originalDailyLateFee,
                    originalBorrowingApprovalRequired,
                    originalAccountApprovalRequired,
                    originalEmailNotificationsEnabled
            );
        }
    }

    @Test
    void user_shouldNotUpdateOrganizationSettings()
            throws Exception {

        User user = createUser(UserRole.USER);

        mockMvc.perform(
                put("/api/organization-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "organizationName": "Unauthorized Update",
                                    "resourceLabel": "Equipment",
                                    "maxActiveBorrowings": 5,
                                    "borrowingDurationDays": 14,
                                    "dailyLateFee": 75.00,
                                    "borrowingApprovalRequired": false,
                                    "accountApprovalRequired": false,
                                    "emailNotificationsEnabled": true
                                }
                                """)
                        .with(userJwt(user))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUser_shouldNotUpdateOrganizationSettings()
            throws Exception {

        mockMvc.perform(
                put("/api/organization-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "organizationName": "Unauthorized Update",
                                    "resourceLabel": "Equipment",
                                    "maxActiveBorrowings": 5,
                                    "borrowingDurationDays": 14,
                                    "dailyLateFee": 75.00,
                                    "borrowingApprovalRequired": false,
                                    "accountApprovalRequired": false,
                                    "emailNotificationsEnabled": true
                                }
                                """)
        )
        .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // SETTINGS VALIDATION
    // =========================================================

    @Test
    void admin_shouldNotUpdateSettingsWithInvalidValues()
            throws Exception {

        User admin = createUser(UserRole.ADMIN);

        mockMvc.perform(
                put("/api/organization-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "organizationName": "",
                                    "resourceLabel": "Equipment",
                                    "maxActiveBorrowings": 0,
                                    "borrowingDurationDays": 0,
                                    "dailyLateFee": -1.00,
                                    "borrowingApprovalRequired": false,
                                    "accountApprovalRequired": false,
                                    "emailNotificationsEnabled": true
                                }
                                """)
                        .with(adminJwt(admin))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    // =========================================================
    // TEST HELPERS
    // =========================================================

    private User createUser(UserRole role) {

        User user = new User();

        user.setFullName("Test " + role + " " + System.nanoTime());
        user.setEmail(
                role.name().toLowerCase()
                        + "-" + System.nanoTime()
                        + "@example.com"
        );
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setMobileNumber("9876543210");
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }

    private OrganizationSettings getSettings() {

        return settingsRepository
                .findById((short) 1)
                .orElseThrow();
    }

    private void restoreSettings(
            String organizationName,
            String resourceLabel,
            Integer maxActiveBorrowings,
            Integer borrowingDurationDays,
            BigDecimal dailyLateFee,
            boolean borrowingApprovalRequired,
            boolean accountApprovalRequired,
            boolean emailNotificationsEnabled
    ) {

        OrganizationSettings settings = getSettings();

        settings.setOrganizationName(organizationName);
        settings.setResourceLabel(resourceLabel);
        settings.setMaxActiveBorrowings(maxActiveBorrowings);
        settings.setBorrowingDurationDays(borrowingDurationDays);
        settings.setDailyLateFee(dailyLateFee);
        settings.setBorrowingApprovalRequired(
                borrowingApprovalRequired
        );
        settings.setAccountApprovalRequired(
                accountApprovalRequired
        );
        settings.setEmailNotificationsEnabled(
                emailNotificationsEnabled
        );

        settingsRepository.saveAndFlush(settings);
    }

    private RequestPostProcessor userJwt(User user) {

        return jwt()
                .jwt(jwt -> jwt
                        .subject(user.getId().toString())
                        .claim("email", user.getEmail())
                        .claim("role", "USER")
                )
                .authorities(
                        new org.springframework.security.core.authority
                                .SimpleGrantedAuthority("ROLE_USER")
                );
    }

    private RequestPostProcessor adminJwt(User admin) {

        return jwt()
                .jwt(jwt -> jwt
                        .subject(admin.getId().toString())
                        .claim("email", admin.getEmail())
                        .claim("role", "ADMIN")
                )
                .authorities(
                        new org.springframework.security.core.authority
                                .SimpleGrantedAuthority("ROLE_ADMIN")
                );
    }
}
