
package com.ravikanth.asset_management.auth;

import com.ravikanth.asset_management.organization.OrganizationSettings;
import com.ravikanth.asset_management.organization.OrganizationSettingsRepository;
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

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationSettingsRepository settingsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerUser_whenApprovalRequired_shouldCreateUnapprovedUser()
            throws Exception {

        withAccountApprovalSetting(true, () -> {

            String email =
                    "register-pending-" + System.nanoTime()
                            + "@example.com";

            String rawPassword = "password123";

            mockMvc.perform(
                    post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "fullName": "Test User",
                                        "email": "%s",
                                        "password": "%s",
                                        "mobileNumber": "9876543210"
                                    }
                                    """.formatted(email, rawPassword))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fullName").value("Test User"))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.mobileNumber").value("9876543210"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.accountStatus").value("UNAPPROVED"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());

            User savedUser = userRepository.findByEmail(email)
                    .orElseThrow();

            assertNotNull(savedUser.getPasswordHash());
            assertFalse(savedUser.getPasswordHash().equals(rawPassword));

            assertTrue(
                    passwordEncoder.matches(
                            rawPassword,
                            savedUser.getPasswordHash()
                    )
            );
        });
    }

    @Test
    void registerUser_whenApprovalNotRequired_shouldCreateActiveUser()
            throws Exception {

        withAccountApprovalSetting(false, () -> {

            String email =
                    "register-active-" + System.nanoTime()
                            + "@example.com";

            mockMvc.perform(
                    post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "fullName": "Active Test User",
                                        "email": "%s",
                                        "password": "password123",
                                        "mobileNumber": "9876543210"
                                    }
                                    """.formatted(email))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));

            User savedUser = userRepository.findByEmail(email)
                    .orElseThrow();

            assertTrue(
                    passwordEncoder.matches(
                            "password123",
                            savedUser.getPasswordHash()
                    )
            );
        });
    }

    @Test
    void loginUser_shouldReturnJwtForActiveUser() throws Exception {

        String email =
                "login-" + System.nanoTime() + "@example.com";

        String rawPassword = "password123";

        User user = createUser(
                "Login Test User",
                email,
                rawPassword,
                UserRole.USER,
                AccountStatus.ACTIVE
        );

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "%s"
                                }
                                """.formatted(email, rawPassword))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token", not(emptyOrNullString())))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.userId").value(user.getId()))
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void loginUser_shouldRejectUnapprovedUser() throws Exception {

        String email =
                "unapproved-login-" + System.nanoTime()
                        + "@example.com";

        createUser(
                "Unapproved User",
                email,
                "password123",
                UserRole.USER,
                AccountStatus.UNAPPROVED
        );

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "password123"
                                }
                                """.formatted(email))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("Account is not active"));
    }

    @Test
    void loginUser_shouldRejectBlockedUser() throws Exception {

        String email =
                "blocked-login-" + System.nanoTime()
                        + "@example.com";

        createUser(
                "Blocked User",
                email,
                "password123",
                UserRole.USER,
                AccountStatus.BLOCKED
        );

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "password123"
                                }
                                """.formatted(email))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("Account is not active"));
    }

    @Test
    void protectedEndpoint_withoutToken_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidToken_shouldReturnAuthenticatedUser()
            throws Exception {

        String email =
                "protected-" + System.nanoTime() + "@example.com";

        User user = createUser(
                "Protected Endpoint User",
                email,
                "password123",
                UserRole.USER,
                AccountStatus.ACTIVE
        );

        mockMvc.perform(
                get("/api/users/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(user.getId().toString())
                                .claim("email", user.getEmail())
                                .claim("role", "USER")
                        ))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId()))
        .andExpect(jsonPath("$.fullName")
                .value("Protected Endpoint User"))
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.mobileNumber")
                .value("9876543210"))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void adminEndpoint_withUserToken_shouldReturnForbidden()
            throws Exception {

        String email =
                "user-admin-" + System.nanoTime() + "@example.com";

        User user = createUser(
                "Regular User",
                email,
                "password123",
                UserRole.USER,
                AccountStatus.ACTIVE
        );

        mockMvc.perform(
                get("/api/admin/dashboard")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject(user.getId().toString())
                                        .claim("email", user.getEmail())
                                        .claim("role", "USER")
                                )
                                .authorities(
                                        new org.springframework.security.core.authority
                                                .SimpleGrantedAuthority("ROLE_USER")
                                )
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_withAdminToken_shouldReturnOk()
            throws Exception {

        String email =
                "admin-" + System.nanoTime() + "@example.com";

        User user = createUser(
                "Admin User",
                email,
                "password123",
                UserRole.ADMIN,
                AccountStatus.ACTIVE
        );

        mockMvc.perform(
                get("/api/admin/dashboard")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject(user.getId().toString())
                                        .claim("email", user.getEmail())
                                        .claim("role", "ADMIN")
                                )
                                .authorities(
                                        new org.springframework.security.core.authority
                                                .SimpleGrantedAuthority("ROLE_ADMIN")
                                )
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value("Admin dashboard"));
    }

    private User createUser(
            String fullName,
            String email,
            String rawPassword,
            UserRole role,
            AccountStatus accountStatus
    ) {
        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setMobileNumber("9876543210");
        user.setRole(role);
        user.setAccountStatus(accountStatus);

        return userRepository.save(user);
    }

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
