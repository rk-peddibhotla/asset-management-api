
package com.ravikanth.asset_management.user;

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
    void student_shouldNotGetPendingUsers() throws Exception {

        User student = createActiveStudent();

        mockMvc.perform(
                get("/api/admin/users/pending")
                        .with(studentJwt(student))
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

        User student = createActiveStudent();
        User admin = createAdmin();

        mockMvc.perform(
                patch(
                        "/api/admin/users/{userId}/approve",
                        student.getId()
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
    void student_shouldNotApproveUser() throws Exception {

        User student = createActiveStudent();
        User pendingUser = createPendingUser();

        mockMvc.perform(
                patch(
                        "/api/admin/users/{userId}/approve",
                        pendingUser.getId()
                )
                .with(studentJwt(student))
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

        User student = createActiveStudent();
        User admin = createAdmin();

        mockMvc.perform(
                patch(
                        "/api/admin/users/{userId}/block",
                        student.getId()
                )
                .with(adminJwt(admin))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value(student.getId()))
        .andExpect(jsonPath("$.email")
                .value(student.getEmail()))
        .andExpect(jsonPath("$.accountStatus")
                .value("BLOCKED"));

        User blockedUser =
                userRepository.findById(student.getId())
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
    void student_shouldNotBlockUser() throws Exception {

        User student = createActiveStudent();
        User anotherStudent = createActiveStudent();

        mockMvc.perform(
                patch(
                        "/api/admin/users/{userId}/block",
                        anotherStudent.getId()
                )
                .with(studentJwt(student))
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


    private JwtRequestPostProcessor studentJwt(User student) {

        return jwt()
                .jwt(jwt -> jwt
                        .subject(student.getId().toString())
                        .claim("email", student.getEmail())
                        .claim("role", "STUDENT")
                )
                .authorities(
                        new SimpleGrantedAuthority("ROLE_STUDENT")
                );
    }


    // ---------------------------------------------------------
    // Test user creation methods
    // ---------------------------------------------------------

    private User createPendingUser() {

        User user = new User();

        user.setFullName(
                "Pending User " + System.nanoTime()
        );

        user.setEmail(
                "pending-" + System.nanoTime()
                        + "@example.com"
        );

        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        user.setMobileNumber("9876543210");
        user.setRole(UserRole.STUDENT);
        user.setAccountStatus(AccountStatus.UNAPPROVED);

        return userRepository.save(user);
    }


    private User createActiveStudent() {

        User user = new User();

        user.setFullName(
                "Active Student " + System.nanoTime()
        );

        user.setEmail(
                "active-" + System.nanoTime()
                        + "@example.com"
        );

        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        user.setMobileNumber("9876543210");
        user.setRole(UserRole.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }


    private User createBlockedUser() {

        User user = new User();

        user.setFullName(
                "Blocked User " + System.nanoTime()
        );

        user.setEmail(
                "blocked-" + System.nanoTime()
                        + "@example.com"
        );

        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        user.setMobileNumber("9876543210");
        user.setRole(UserRole.STUDENT);
        user.setAccountStatus(AccountStatus.BLOCKED);

        return userRepository.save(user);
    }


    private User createAdmin() {

        User user = new User();

        user.setFullName(
                "Test Admin " + System.nanoTime()
        );

        user.setEmail(
                "admin-" + System.nanoTime()
                        + "@example.com"
        );

        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        user.setMobileNumber("9876543210");
        user.setRole(UserRole.ADMIN);
        user.setAccountStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }
}

