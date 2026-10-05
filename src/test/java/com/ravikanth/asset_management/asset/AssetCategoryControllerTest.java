
package com.ravikanth.asset_management.asset;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssetCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AssetCategoryRepository categoryRepository;

    @Test
    void admin_shouldCreateCategory() throws Exception {

        User admin = createUser(
                "admin-category-" + System.nanoTime() + "@example.com",
                UserRole.ADMIN
        );

        String category = "Laptop-" + System.nanoTime();

        mockMvc.perform(
                post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "category": "%s",
                                    "subCategory": "Dell"
                                }
                                """.formatted(category))
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject(admin.getId().toString())
                                        .claim("email", admin.getEmail())
                                        .claim("role", "ADMIN")
                                )
                                .authorities(
                                        new org.springframework.security.core.authority
                                                .SimpleGrantedAuthority("ROLE_ADMIN")
                                )
                        )
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.category").value(category))
        .andExpect(jsonPath("$.subCategory").value("Dell"));
    }

    @Test
    void student_shouldNotCreateCategory() throws Exception {

        User student = createUser(
                "student-category-" + System.nanoTime() + "@example.com",
                UserRole.STUDENT
        );

        String category = "Laptop-" + System.nanoTime();

        mockMvc.perform(
                post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "category": "%s",
                                    "subCategory": "HP"
                                }
                                """.formatted(category))
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject(student.getId().toString())
                                        .claim("email", student.getEmail())
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
    void authenticatedUser_shouldListCategories() throws Exception {

        User student = createUser(
                "list-category-" + System.nanoTime() + "@example.com",
                UserRole.STUDENT
        );

        AssetCategory category = new AssetCategory();

        category.setCategory("Monitor-" + System.nanoTime());
        category.setSubCategory("Dell");

        AssetCategory savedCategory =
                categoryRepository.save(category);

        assertNotNull(savedCategory.getId());

        mockMvc.perform(
                get("/api/categories")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject(student.getId().toString())
                                        .claim("email", student.getEmail())
                                        .claim("role", "STUDENT")
                                )
                                .authorities(
                                        new org.springframework.security.core.authority
                                                .SimpleGrantedAuthority("ROLE_STUDENT")
                                )
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").isNumber());
    }

    @Test
    void unauthenticatedUser_shouldNotListCategories()
            throws Exception {

        mockMvc.perform(
                get("/api/categories")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateCategory_shouldBeRejected() throws Exception {

        User admin = createUser(
                "duplicate-category-" + System.nanoTime()
                        + "@example.com",
                UserRole.ADMIN
        );

        String category = "Keyboard-" + System.nanoTime();

        String requestBody = """
                {
                    "category": "%s",
                    "subCategory": "Logitech"
                }
                """.formatted(category);

        mockMvc.perform(
                post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject(admin.getId().toString())
                                        .claim("email", admin.getEmail())
                                        .claim("role", "ADMIN")
                                )
                                .authorities(
                                        new org.springframework.security.core.authority
                                                .SimpleGrantedAuthority("ROLE_ADMIN")
                                )
                        )
        )
        .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject(admin.getId().toString())
                                        .claim("email", admin.getEmail())
                                        .claim("role", "ADMIN")
                                )
                                .authorities(
                                        new org.springframework.security.core.authority
                                                .SimpleGrantedAuthority("ROLE_ADMIN")
                                )
                        )
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("Asset category already exists"));
    }

    private User createUser(
            String email,
            UserRole role
    ) {

        User user = new User();

        user.setFullName(
                role == UserRole.ADMIN
                        ? "Test Admin"
                        : "Test Student"
        );

        user.setEmail(email);

        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        user.setMobileNumber("9876543210");

        user.setRole(role);

        user.setAccountStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }
}

