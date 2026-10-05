
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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AssetCategoryRepository categoryRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Test
    void admin_shouldCreateAsset() throws Exception {

        User admin = createUser(
                "admin-asset-" + System.nanoTime() + "@example.com",
                UserRole.ADMIN
        );

        AssetCategory category = createCategory();

        mockMvc.perform(
                post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Dell Latitude 5540",
                                    "manufacturer": "Dell",
                                    "price": 85000.00,
                                    "categoryId": %d
                                }
                                """.formatted(category.getId()))
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
        .andExpect(jsonPath("$.name")
                .value("Dell Latitude 5540"))
        .andExpect(jsonPath("$.manufacturer")
                .value("Dell"))
        .andExpect(jsonPath("$.price").value(85000.00))
        .andExpect(jsonPath("$.categoryId")
                .value(category.getId()))
        .andExpect(jsonPath("$.category")
                .value(category.getCategory()))
        .andExpect(jsonPath("$.subCategory")
                .value(category.getSubCategory()));
    }

    @Test
    void student_shouldNotCreateAsset() throws Exception {

        User student = createUser(
                "student-asset-" + System.nanoTime() + "@example.com",
                UserRole.STUDENT
        );

        AssetCategory category = createCategory();

        mockMvc.perform(
                post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "HP EliteBook",
                                    "manufacturer": "HP",
                                    "price": 70000.00,
                                    "categoryId": %d
                                }
                                """.formatted(category.getId()))
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
    void admin_shouldNotCreateAssetWithInvalidCategory()
            throws Exception {

        User admin = createUser(
                "invalid-category-" + System.nanoTime()
                        + "@example.com",
                UserRole.ADMIN
        );

        Long invalidCategoryId = 999999999L;

        mockMvc.perform(
                post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "MacBook Pro",
                                    "manufacturer": "Apple",
                                    "price": 150000.00,
                                    "categoryId": %d
                                }
                                """.formatted(invalidCategoryId))
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
                .value("Asset category not found"));
    }

    @Test
    void authenticatedUser_shouldListAssets() throws Exception {

        User student = createUser(
                "list-assets-" + System.nanoTime() + "@example.com",
                UserRole.STUDENT
        );

        AssetCategory category = createCategory();

        Asset asset = new Asset();

        asset.setName("Lenovo ThinkPad");
        asset.setManufacturer("Lenovo");
        asset.setPrice(new BigDecimal("90000.00"));
        asset.setCategory(category);

        Asset savedAsset = assetRepository.save(asset);

        assertNotNull(savedAsset.getId());

        mockMvc.perform(
                get("/api/assets")
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
    void unauthenticatedUser_shouldNotListAssets()
            throws Exception {

        mockMvc.perform(
                get("/api/assets")
        )
        .andExpect(status().isUnauthorized());
    }

    private AssetCategory createCategory() {

        AssetCategory category = new AssetCategory();

        category.setCategory(
                "Category-" + System.nanoTime()
        );

        category.setSubCategory(
                "SubCategory-" + System.nanoTime()
        );

        return categoryRepository.save(category);
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

