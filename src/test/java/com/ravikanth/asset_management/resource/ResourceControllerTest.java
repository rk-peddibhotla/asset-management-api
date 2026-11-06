
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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ResourceCategoryRepository categoryRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    // ---------------------------------------------------------
    // Create resource
    // ---------------------------------------------------------

    @Test
    void admin_shouldCreateResource() throws Exception {

        User admin = createUser(
                "admin-resource-" + System.nanoTime() + "@example.com",
                UserRole.ADMIN
        );

        ResourceCategory category = createCategory();

        mockMvc.perform(
                post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Dell Latitude 5540",
                                    "manufacturer": "Dell",
                                    "purchasePrice": 85000.00,
                                    "description": "Business laptop",
                                    "categoryId": %d
                                }
                                """.formatted(category.getId()))
                        .with(adminJwt(admin))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.name")
                .value("Dell Latitude 5540"))
        .andExpect(jsonPath("$.manufacturer")
                .value("Dell"))
        .andExpect(jsonPath("$.purchasePrice").value(85000.00))
        .andExpect(jsonPath("$.description")
                .value("Business laptop"))
        .andExpect(jsonPath("$.status")
                .value("AVAILABLE"))
        .andExpect(jsonPath("$.categoryId")
                .value(category.getId()))
        .andExpect(jsonPath("$.category")
                .value(category.getCategory()))
        .andExpect(jsonPath("$.subCategory")
                .value(category.getSubCategory()));
    }

    @Test
    void user_shouldNotCreateResource() throws Exception {

        User user = createUser(
                "user-resource-" + System.nanoTime() + "@example.com",
                UserRole.USER
        );

        ResourceCategory category = createCategory();

        mockMvc.perform(
                post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "HP EliteBook",
                                    "manufacturer": "HP",
                                    "purchasePrice": 70000.00,
                                    "description": "Business laptop",
                                    "categoryId": %d
                                }
                                """.formatted(category.getId()))
                        .with(userJwt(user))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void admin_shouldNotCreateResourceWithInvalidCategory()
            throws Exception {

        User admin = createUser(
                "invalid-category-" + System.nanoTime()
                        + "@example.com",
                UserRole.ADMIN
        );

        Long invalidCategoryId = 999999999L;

        mockMvc.perform(
                post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "MacBook Pro",
                                    "manufacturer": "Apple",
                                    "purchasePrice": 150000.00,
                                    "description": "Development laptop",
                                    "categoryId": %d
                                }
                                """.formatted(invalidCategoryId))
                        .with(adminJwt(admin))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("Resource category not found"));
    }

    // ---------------------------------------------------------
    // Get resources
    // ---------------------------------------------------------

    @Test
    void authenticatedUser_shouldListResources() throws Exception {

        User user = createUser(
                "list-resources-" + System.nanoTime() + "@example.com",
                UserRole.USER
        );

        ResourceCategory category = createCategory();

        Resource resource = new Resource();

        resource.setName("Lenovo ThinkPad");
        resource.setManufacturer("Lenovo");
        resource.setPurchasePrice(new BigDecimal("90000.00"));
        resource.setDescription("Development laptop");
        resource.setCategory(category);

        Resource savedResource = resourceRepository.save(resource);

        assertNotNull(savedResource.getId());

        mockMvc.perform(
                get("/api/resources")
                        .with(userJwt(user))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath(
                "$[?(@.id == %d)].name"
                        .formatted(savedResource.getId())
        ).value(savedResource.getName()))
        .andExpect(jsonPath(
                "$[?(@.id == %d)].status"
                        .formatted(savedResource.getId())
        ).value("AVAILABLE"))
        .andExpect(jsonPath(
                "$[?(@.id == %d)].category"
                        .formatted(savedResource.getId())
        ).value(category.getCategory()));
    }

    @Test
    void unauthenticatedUser_shouldNotListResources()
            throws Exception {

        mockMvc.perform(
                get("/api/resources")
        )
        .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // Test category creation
    // ---------------------------------------------------------

    private ResourceCategory createCategory() {

        ResourceCategory category = new ResourceCategory();

        category.setCategory(
                "Category-" + System.nanoTime()
        );

        category.setSubCategory(
                "SubCategory-" + System.nanoTime()
        );

        return categoryRepository.save(category);
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
