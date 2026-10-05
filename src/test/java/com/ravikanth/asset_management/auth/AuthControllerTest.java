
package com.ravikanth.asset_management.auth;

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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerUser_shouldCreateUnapprovedStudent() throws Exception {

        String email = "test-" + System.nanoTime() + "@example.com";
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
        .andExpect(jsonPath("$.role").value("STUDENT"))
        .andExpect(jsonPath("$.accountStatus").value("UNAPPROVED"))
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist());

        User savedUser = userRepository.findByEmail(email)
                .orElseThrow();

        assertNotNull(savedUser.getPasswordHash());

        assertFalse(
                savedUser.getPasswordHash().equals(rawPassword)
        );

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        savedUser.getPasswordHash()
                )
        );
    }

    @Test
    void loginUser_shouldReturnJwtForActiveUser() throws Exception {

        String email = "login-" + System.nanoTime() + "@example.com";
        String rawPassword = "password123";

        User user = new User();

        user.setFullName("Login Test User");
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(rawPassword)
        );
        user.setMobileNumber("9876543210");
        user.setRole(UserRole.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);

        userRepository.save(user);

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
        .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void loginUser_shouldRejectUnapprovedUser() throws Exception {

        String email =
                "unapproved-login-" + System.nanoTime()
                        + "@example.com";

        String rawPassword = "password123";

        User user = new User();

        user.setFullName("Unapproved User");
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(rawPassword)
        );
        user.setMobileNumber("9876543210");
        user.setRole(UserRole.STUDENT);
        user.setAccountStatus(AccountStatus.UNAPPROVED);

        userRepository.save(user);

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

        String rawPassword = "password123";

        User user = new User();

        user.setFullName("Blocked User");
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(rawPassword)
        );
        user.setMobileNumber("9876543210");
        user.setRole(UserRole.STUDENT);
        user.setAccountStatus(AccountStatus.BLOCKED);

        userRepository.save(user);

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

        String rawPassword = "password123";

        User user = new User();

        user.setFullName("Protected Endpoint User");
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(rawPassword)
        );
        user.setMobileNumber("9876543210");
        user.setRole(UserRole.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);

        userRepository.save(user);

        mockMvc.perform(
                get("/api/users/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(user.getId().toString())
                                .claim("email", user.getEmail())
                                .claim("role", "STUDENT")
                        ))
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$").value(
                        "Authenticated as: " + user.getId()
                )
        );
    }

    @Test
    void adminEndpoint_withStudentToken_shouldReturnForbidden()
            throws Exception {

        String email =
                "student-admin-" + System.nanoTime() + "@example.com";

        User user = new User();

        user.setFullName("Student User");
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );
        user.setMobileNumber("9876543210");
        user.setRole(UserRole.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);

        userRepository.save(user);

        mockMvc.perform(
                get("/api/admin/dashboard")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject(user.getId().toString())
                                        .claim("email", user.getEmail())
                                        .claim("role", "STUDENT")
                                )
                                .authorities(
                                        new org.springframework.security.core.authority
                                                .SimpleGrantedAuthority("ROLE_STUDENT")
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

        User user = new User();

        user.setFullName("Admin User");
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );
        user.setMobileNumber("9876543210");
        user.setRole(UserRole.ADMIN);
        user.setAccountStatus(AccountStatus.ACTIVE);

        userRepository.save(user);

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
        .andExpect(
                jsonPath("$").value("Admin dashboard")
        );
    }
}

