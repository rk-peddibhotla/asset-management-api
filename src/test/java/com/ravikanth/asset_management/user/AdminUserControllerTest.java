
package com.ravikanth.asset_management.user;

import com.ravikanth.asset_management.organization.OrganizationSettings;
import com.ravikanth.asset_management.organization.OrganizationSettingsRepository;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationSettingsRepository settingsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ---------------------------------------------------------
    // Get pending users
    // ---------------------------------------------------------

    @Test
    void admin_shouldGetPendingUsers() throws Exception {

        User pendingUser = createPendingUser();
        User admin = createAdmin();

        mockMvc.perform(
                get("/api/admin/users/pending")
                        .with(adminJwt(admin))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$[?(@.id == %d)].fullName"
                        .formatted(pendingUser.getId())
        ).value(pendingUser.getFullName()))
        .andExpect(jsonPath(
                "$[?(@.id == %d)].email"
                        .formatted(pendingUser.getId())
        ).value(pendingUser.getEmail()))
        .andExpect(jsonPath(
                "$[?(@.id == %d)].accountStatus"
                        .formatted(pendingUser.getId())
        ).value("UNAPPROVED"));
    }

    @Test
    void user_shouldNotGetPendingUsers() throws Exception {

        User user = createActiveUser();

        mockMvc.perform(
                get("/api/admin/users/pending")
                        .with(userJwt(user))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUser_shouldNotGetPendingUsers()
            throws Exception {

        mockMvc.perform(
                get("/api/admin/users/pending")
        )
        .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // Approve users
    // ---------------------------------------------------------

    @Test
    void admin_shouldApprovePendingUser() throws Exception {

        User pendingUser = createPendingUser();
        User admin = createAdmin();

        mockMvc.perform(
                patch(
                        "/api/admin/users/{userId}/approve",
                        pendingUser.getId()
                )
                .with(adminJwt(admin))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value(pendingUser.getId()))
        .andExpect(jsonPath("$.email")
                .value(pendingUser.getEmail()))
        .andExpect(jsonPath("$.accountStatus")
                .value("ACTIVE"));

        User approvedUser =
                userRepository.findById(pendingUser.getId())
                        .orElseThrow();

        assertEquals(
                AccountStatus.ACTIVE,
                approvedUser.getAccountStatus()
        );
    }

    @Test
    void admin_shouldNotApproveAlreadyActiveUser()
            throws Exception {

        User user = createActiveUser();
        User admin = createAdmin();

        mockMvc.perform(
                patch(
                        "/api/admin/users/{userId}/approve",
                        user.getId()
                )
                .with(adminJwt(admin))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("Only unapproved users can be approved"));
    }

    @Test
    void disablingAccountApproval_shouldNotApproveExistingPendingUsers()
            throws Exception {

        User pendingUser = createPendingUser();

        withAccountApprovalSetting(false, () -> {

            User savedUser = userRepository
                    .findById(pendingUser.getId())
                    .orElseThrow();

            assertEquals(
                    AccountStatus.UNAPPROVED,
                    savedUser.getAccountStatus()
            );
        });
    }

    @Test
    void user_shouldNotApproveAnotherUser() throws Exception {

        User user = createActiveUser();
        User pendingUser = createPendingUser();

        mockMvc.perform(
                patch(
                        "/api/admin/users/{userId}/approve",
                        pendingUser.getId()
                )
                .with(userJwt(user))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void admin_shouldReturnBadRequestForMissingUser()
            throws Exception {

        User admin = createAdmin();

        mockMvc.perform(
                patch(
                        "/api/admin/users/{userId}/approve",
                        999999999L
                )
                .with(adminJwt(admin))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("User not found"));
    }

    // ---------------------------------------------------------
    // Block users
    // ---------------------------------------------------------

    @Test
    void admin_shouldBlockActiveUser() throws Exception {

        User user = createActiveUser();
        User admin = createAdmin();

        mockMvc.perform(
                patch(
                        "/api/admin/users/{userId}/block",
                        user.getId()
                )
                .with(adminJwt(admin))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value(user.getId()))
        .andExpect(jsonPath("$.email")
                .value(user.getEmail()))
        .andExpect(jsonPath("$.accountStatus")
                .value("BLOCKED"));

        User blockedUser =
                userRepository.findById(user.getId())
                        .orElseThrow();

        assertEquals(
                AccountStatus.BLOCKED,
                blockedUser.getAccountStatus()
        );
    }

    @Test
    void admin_shouldNotBlockAlreadyBlockedUser()
            throws Exception {

        User blockedUser = createBlockedUser();
        User admin = createAdmin();

        mockMvc.perform(
                patch(
                        "/api/admin/users/{userId}/block",
                        blockedUser.getId()
                )
                .with(adminJwt(admin))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("User is already blocked"));
    }

    @Test
    void user_shouldNotBlockAnotherUser() throws Exception {

        User user = createActiveUser();
        User anotherUser = createActiveUser();

        mockMvc.perform(
                patch(
                        "/api/admin/users/{userId}/block",
                        anotherUser.getId()
                )
                .with(userJwt(user))
        )
        .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // JWT helper methods
    // ---------------------------------------------------------

    private JwtRequestPostProcessor adminJwt(User admin) {

        return jwt()
                .jwt(jwt -> jwt
                        .subject(admin.getId().toString())
                        .claim("email", admin.getEmail())
                        .claim("role", "ADMIN")
                )
                .authorities(
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                );
    }

    private JwtRequestPostProcessor userJwt(User user) {

        return jwt()
                .jwt(jwt -> jwt
                        .subject(user.getId().toString())
                        .claim("email", user.getEmail())
                        .claim("role", "USER")
                )
                .authorities(
                        new SimpleGrantedAuthority("ROLE_USER")
                );
    }

    // ---------------------------------------------------------
    // Test user creation methods
    // ---------------------------------------------------------

    private User createPendingUser() {

        return createUser(
                "Pending User",
                "pending-",
                AccountStatus.UNAPPROVED,
                UserRole.USER
        );
    }

    private User createActiveUser() {

        return createUser(
                "Active User",
                "active-",
                AccountStatus.ACTIVE,
                UserRole.USER
        );
    }

    private User createBlockedUser() {

        return createUser(
                "Blocked User",
                "blocked-",
                AccountStatus.BLOCKED,
                UserRole.USER
        );
    }

    private User createAdmin() {

        return createUser(
                "Test Admin",
                "admin-",
                AccountStatus.ACTIVE,
                UserRole.ADMIN
        );
    }

    private User createUser(
            String fullNamePrefix,
            String emailPrefix,
            AccountStatus accountStatus,
            UserRole role
    ) {

        User user = new User();

        user.setFullName(
                fullNamePrefix + " " + System.nanoTime()
        );

        user.setEmail(
                emailPrefix + System.nanoTime()
                        + "@example.com"
        );

        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        user.setMobileNumber("9876543210");
        user.setRole(role);
        user.setAccountStatus(accountStatus);

        return userRepository.save(user);
    }

    // ---------------------------------------------------------
    // Organization settings helper
    // ---------------------------------------------------------

    private void withAccountApprovalSetting(
            boolean required,
            ThrowingRunnable action
    ) throws Exception {

        OrganizationSettings settings = settingsRepository
                .findById((short) 1)
                .orElseThrow();

        boolean originalValue =
                settings.isAccountApprovalRequired();

        try {
            settings.setAccountApprovalRequired(required);
            settingsRepository.saveAndFlush(settings);

            action.run();

        } finally {
            settings.setAccountApprovalRequired(originalValue);
            settingsRepository.saveAndFlush(settings);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
