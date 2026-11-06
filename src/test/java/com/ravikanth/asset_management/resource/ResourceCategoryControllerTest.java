
package com.ravikanth.asset_management.resource;

import com.ravikanth.asset_management.user.AccountStatus;
import com.ravikanth.asset_management.user.User;
import com.ravikanth.asset_management.user.UserRepository;
import com.ravikanth.asset_management.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
class ResourceCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ResourceCategoryRepository categoryRepository;

    // ---------------------------------------------------------
    // Create category
    // ---------------------------------------------------------

    @Test
    void admin_shouldCreateCategory() throws Exception {

        User admin = createUser(
                "admin-category-" + System.nanoTime() + "@example.com",
                UserRole.ADMIN
        );

        String category = "Laptop-" + System.nanoTime();

        mockMvc.perform(
                post("/api/resource-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "category": "%s",
                                    "subCategory": "Dell"
                                }
                                """.formatted(category))
                        .with(adminJwt(admin))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.category").value(category))
        .andExpect(jsonPath("$.subCategory").value("Dell"));
    }

    @Test
    void user_shouldNotCreateCategory() throws Exception {

        User user = createUser(
                "user-category-" + System.nanoTime() + "@example.com",
                UserRole.USER
        );

        String category = "Laptop-" + System.nanoTime();

        mockMvc.perform(
                post("/api/resource-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "category": "%s",
                                    "subCategory": "HP"
                                }
                                """.formatted(category))
                        .with(userJwt(user))
        )
        .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // Get categories
    // ---------------------------------------------------------

    @Test
    void authenticatedUser_shouldListCategories() throws Exception {

        User user = createUser(
                "list-category-" + System.nanoTime() + "@example.com",
                UserRole.USER
        );

        ResourceCategory category = new ResourceCategory();

        category.setCategory("Monitor-" + System.nanoTime());
        category.setSubCategory("Dell");

        ResourceCategory savedCategory =
                categoryRepository.save(category);

        assertNotNull(savedCategory.getId());

        mockMvc.perform(
                get("/api/resource-categories")
                        .with(userJwt(user))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath(
                "$[?(@.id == %d)].category"
                        .formatted(savedCategory.getId())
        ).value(savedCategory.getCategory()));
    }

    @Test
    void unauthenticatedUser_shouldNotListCategories()
            throws Exception {

        mockMvc.perform(
                get("/api/resource-categories")
        )
        .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // Duplicate category
    // ---------------------------------------------------------

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
                post("/api/resource-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(adminJwt(admin))
        )
        .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/resource-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(adminJwt(admin))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("Resource category already exists"));
    }

    // ---------------------------------------------------------
    // JWT helper methods
    // ---------------------------------------------------------

    private org.springframework.security.test.web.servlet.request
            .SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
    adminJwt(User admin) {

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

    private org.springframework.security.test.web.servlet.request
            .SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor
    userJwt(User user) {

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
    // Test user creation
    // ---------------------------------------------------------

    private User createUser(
            String email,
            UserRole role
    ) {

        User user = new User();

        user.setFullName(
                role == UserRole.ADMIN
                        ? "Test Admin"
                        : "Test User"
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
