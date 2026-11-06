
package com.ravikanth.asset_management.borrowing;

import com.ravikanth.asset_management.organization.OrganizationSettings;
import com.ravikanth.asset_management.organization.OrganizationSettingsRepository;
import com.ravikanth.asset_management.resource.Resource;
import com.ravikanth.asset_management.resource.ResourceCategory;
import com.ravikanth.asset_management.resource.ResourceCategoryRepository;
import com.ravikanth.asset_management.resource.ResourceRepository;
import com.ravikanth.asset_management.resource.ResourceStatus;
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
import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BorrowingControllerTest {

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

    @Autowired
    private BorrowingRequestRepository borrowingRequestRepository;

    @Autowired
    private OrganizationSettingsRepository settingsRepository;

    // =========================================================
    // CREATE BORROWING
    // =========================================================

    @Test
    void authenticatedUser_shouldCreatePendingBorrowing()
            throws Exception {

        User user = createUser();
        Resource resource = createResource();

        withSettings(
                settings -> {
                    settings.setBorrowingApprovalRequired(true);
                    settings.setBorrowingDurationDays(10);
                    settings.setDailyLateFee(new BigDecimal("50.00"));
                },
                () -> {
                    mockMvc.perform(
                            post("/api/borrowings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "resourceId": %d
                                            }
                                            """.formatted(resource.getId()))
                                    .with(userJwt(user))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.userId").value(user.getId()))
                    .andExpect(jsonPath("$.resourceId").value(resource.getId()))
                    .andExpect(jsonPath("$.resourceName").value(resource.getName()))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.requestedAt").isNotEmpty())
                    .andExpect(jsonPath("$.activatedAt").doesNotExist())
                    .andExpect(jsonPath("$.dueAt").doesNotExist())
                    .andExpect(jsonPath("$.returnedAt").doesNotExist())
                    .andExpect(jsonPath("$.lateFeeAmount").value(0));

                    BorrowingRequest saved =
                            borrowingRequestRepository
                                    .findAllByUserIdWithDetails(user.getId())
                                    .stream()
                                    .filter(item ->
                                            item.getResource().getId()
                                                    .equals(resource.getId())
                                    )
                                    .findFirst()
                                    .orElseThrow();

                    assertEquals(
                            new BigDecimal("50.00"),
                            saved.getDailyLateFeeSnapshot()
                    );

                    assertEquals(
                            10,
                            saved.getBorrowingDurationDaysSnapshot()
                    );
                }
        );
    }

    @Test
    void unauthenticatedUser_shouldNotCreateBorrowing()
            throws Exception {

        Resource resource = createResource();

        mockMvc.perform(
                post("/api/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "resourceId": %d
                                }
                                """.formatted(resource.getId()))
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUser_shouldNotCreateBorrowingForMissingResource()
            throws Exception {

        User user = createUser();

        mockMvc.perform(
                post("/api/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "resourceId": 999999999
                                }
                                """)
                        .with(userJwt(user))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void userWithThreeActiveBorrowings_shouldNotCreateFourth()
            throws Exception {

        User user = createUser();

        Resource resource1 = createResource();
        Resource resource2 = createResource();
        Resource resource3 = createResource();
        Resource resource4 = createResource();

        createBorrowing(user, resource1, BorrowingStatus.ACTIVE);
        createBorrowing(user, resource2, BorrowingStatus.ACTIVE);
        createBorrowing(user, resource3, BorrowingStatus.ACTIVE);

        withSettings(
                settings -> {
                    settings.setBorrowingApprovalRequired(false);
                    settings.setMaxActiveBorrowings(3);
                },
                () -> {
                    mockMvc.perform(
                            post("/api/borrowings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "resourceId": %d
                                            }
                                            """.formatted(resource4.getId()))
                                    .with(userJwt(user))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message")
                            .value("User cannot have more than 3 active borrowings"));
                }
        );
    }

    @Test
    void configuredBorrowingLimit_shouldBeEnforced()
            throws Exception {

        User user = createUser();

        Resource resource1 = createResource();
        Resource resource2 = createResource();

        createBorrowing(user, resource1, BorrowingStatus.ACTIVE);

        withSettings(
                settings -> {
                    settings.setBorrowingApprovalRequired(false);
                    settings.setMaxActiveBorrowings(1);
                },
                () -> {
                    mockMvc.perform(
                            post("/api/borrowings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "resourceId": %d
                                            }
                                            """.formatted(resource2.getId()))
                                    .with(userJwt(user))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("User cannot have more than 1 active borrowings"));
                }
        );
    }

    @Test
    void borrowingApprovalDisabled_shouldActivateImmediately()
            throws Exception {

        User user = createUser();
        Resource resource = createResource();

        withSettings(
                settings -> {
                    settings.setBorrowingApprovalRequired(false);
                    settings.setBorrowingDurationDays(7);
                    settings.setDailyLateFee(new BigDecimal("25.00"));
                },
                () -> {
                    mockMvc.perform(
                            post("/api/borrowings")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "resourceId": %d
                                            }
                                            """.formatted(resource.getId()))
                                    .with(userJwt(user))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.activatedAt").isNotEmpty())
                    .andExpect(jsonPath("$.dueAt").isNotEmpty())
                    .andExpect(jsonPath("$.lateFeeAmount").value(0));

                    BorrowingRequest saved =
                            borrowingRequestRepository
                                    .findAllByUserIdWithDetails(user.getId())
                                    .stream()
                                    .filter(item ->
                                            item.getResource().getId()
                                                    .equals(resource.getId())
                                    )
                                    .findFirst()
                                    .orElseThrow();

                    assertEquals(
                            7,
                            saved.getBorrowingDurationDaysSnapshot()
                    );

                    assertEquals(
                            new BigDecimal("25.00"),
                            saved.getDailyLateFeeSnapshot()
                    );

                    assertEquals(
                            saved.getActivatedAt().plusDays(7),
                            saved.getDueAt()
                    );
                }
        );
    }

    // =========================================================
    // APPROVE BORROWING
    // =========================================================

    @Test
    void admin_shouldApprovePendingBorrowing()
            throws Exception {

        User user = createUser();
        User admin = createAdmin();

        Resource resource = createResource();

        BorrowingRequest borrowing = createBorrowing(
                user,
                resource,
                BorrowingStatus.PENDING
        );

        mockMvc.perform(
                patch(
                        "/api/admin/borrowings/{borrowingId}/approve",
                        borrowing.getId()
                )
                .with(adminJwt(admin))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(borrowing.getId()))
        .andExpect(jsonPath("$.userId").value(user.getId()))
        .andExpect(jsonPath("$.resourceId").value(resource.getId()))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.requestedAt").isNotEmpty())
        .andExpect(jsonPath("$.activatedAt").isNotEmpty())
        .andExpect(jsonPath("$.dueAt").isNotEmpty())
        .andExpect(jsonPath("$.returnedAt").doesNotExist())
        .andExpect(jsonPath("$.lateFeeAmount").value(0));
    }

    @Test
    void user_shouldNotApproveBorrowing()
            throws Exception {

        User user = createUser();
        Resource resource = createResource();

        BorrowingRequest borrowing = createBorrowing(
                user,
                resource,
                BorrowingStatus.PENDING
        );

        mockMvc.perform(
                patch(
                        "/api/admin/borrowings/{borrowingId}/approve",
                        borrowing.getId()
                )
                .with(userJwt(user))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void admin_shouldNotApproveAlreadyActiveBorrowing()
            throws Exception {

        User user = createUser();
        User admin = createAdmin();

        Resource resource = createResource();

        BorrowingRequest borrowing = createBorrowing(
                user,
                resource,
                BorrowingStatus.ACTIVE
        );

        mockMvc.perform(
                patch(
                        "/api/admin/borrowings/{borrowingId}/approve",
                        borrowing.getId()
                )
                .with(adminJwt(admin))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
                .value("Only pending borrowing requests can be approved"));
    }

    @Test
    void adminApproval_shouldUseDurationSnapshot()
            throws Exception {

        User user = createUser();
        User admin = createAdmin();

        Resource resource = createResource();

        withSettings(
                settings -> {
                    settings.setBorrowingApprovalRequired(true);
                    settings.setBorrowingDurationDays(7);
                    settings.setDailyLateFee(new BigDecimal("25.00"));
                },
                () -> {
                    BorrowingRequest borrowing = createBorrowing(
                            user,
                            resource,
                            BorrowingStatus.PENDING
                    );

                    borrowing.setBorrowingDurationDaysSnapshot(7);
                    borrowing.setDailyLateFeeSnapshot(
                            new BigDecimal("25.00")
                    );
                    borrowingRequestRepository.saveAndFlush(borrowing);

                    withSettings(
                            settings -> {
                                settings.setBorrowingDurationDays(2);
                                settings.setDailyLateFee(
                                        new BigDecimal("100.00")
                                );
                            },
                            () -> {
                                mockMvc.perform(
                                        patch(
                                                "/api/admin/borrowings/{borrowingId}/approve",
                                                borrowing.getId()
                                        )
                                        .with(adminJwt(admin))
                                )
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("ACTIVE"));

                                BorrowingRequest approved =
                                        borrowingRequestRepository
                                                .findById(borrowing.getId())
                                                .orElseThrow();

                                assertEquals(
                                        approved.getActivatedAt().plusDays(7),
                                        approved.getDueAt()
                                );

                                assertEquals(
                                        new BigDecimal("25.00"),
                                        approved.getDailyLateFeeSnapshot()
                                );
                            }
                    );
                }
        );
    }

    // =========================================================
    // REJECT BORROWING
    // =========================================================

    @Test
    void admin_shouldRejectPendingBorrowing()
            throws Exception {

        User user = createUser();
        User admin = createAdmin();

        Resource resource = createResource();

        BorrowingRequest borrowing = createBorrowing(
                user,
                resource,
                BorrowingStatus.PENDING
        );

        mockMvc.perform(
                patch(
                        "/api/admin/borrowings/{borrowingId}/reject",
                        borrowing.getId()
                )
                .with(adminJwt(admin))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(borrowing.getId()))
        .andExpect(jsonPath("$.userId").value(user.getId()))
        .andExpect(jsonPath("$.resourceId").value(resource.getId()))
        .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void user_shouldNotRejectBorrowing()
            throws Exception {

        User user = createUser();
        Resource resource = createResource();

        BorrowingRequest borrowing = createBorrowing(
                user,
                resource,
                BorrowingStatus.PENDING
        );

        mockMvc.perform(
                patch(
                        "/api/admin/borrowings/{borrowingId}/reject",
                        borrowing.getId()
                )
                .with(userJwt(user))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void admin_shouldNotRejectAlreadyActiveBorrowing()
            throws Exception {

        User user = createUser();
        User admin = createAdmin();

        Resource resource = createResource();

        BorrowingRequest borrowing = createBorrowing(
                user,
                resource,
                BorrowingStatus.ACTIVE
        );

        mockMvc.perform(
                patch(
                        "/api/admin/borrowings/{borrowingId}/reject",
                        borrowing.getId()
                )
                .with(adminJwt(admin))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
                .value("Only pending borrowing requests can be rejected"));
    }

    // =========================================================
    // RETURN BORROWING
    // =========================================================

    @Test
    void user_shouldReturnOwnActiveBorrowing()
            throws Exception {

        User user = createUser();
        Resource resource = createResource();

        BorrowingRequest borrowing = createBorrowing(
                user,
                resource,
                BorrowingStatus.ACTIVE
        );

        borrowing.setActivatedAt(LocalDateTime.now().minusDays(5));
        borrowing.setDueAt(LocalDateTime.now().plusDays(5));
        borrowingRequestRepository.save(borrowing);

        mockMvc.perform(
                patch(
                        "/api/borrowings/{borrowingId}/return",
                        borrowing.getId()
                )
                .with(userJwt(user))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(borrowing.getId()))
        .andExpect(jsonPath("$.userId").value(user.getId()))
        .andExpect(jsonPath("$.resourceId").value(resource.getId()))
        .andExpect(jsonPath("$.status").value("RETURNED"))
        .andExpect(jsonPath("$.requestedAt").isNotEmpty())
        .andExpect(jsonPath("$.activatedAt").isNotEmpty())
        .andExpect(jsonPath("$.dueAt").isNotEmpty())
        .andExpect(jsonPath("$.returnedAt").isNotEmpty())
        .andExpect(jsonPath("$.lateFeeAmount").value(0));

        BorrowingRequest returned =
                borrowingRequestRepository.findById(borrowing.getId())
                        .orElseThrow();

        assertNotNull(returned.getReturnedAt());

        assertEquals(
                0,
                returned.getLateFeeAmount().compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void user_shouldNotReturnAnotherUsersBorrowing()
            throws Exception {

        User owner = createUser();
        User anotherUser = createUser();

        Resource resource = createResource();

        BorrowingRequest borrowing = createBorrowing(
                owner,
                resource,
                BorrowingStatus.ACTIVE
        );

        borrowing.setDueAt(LocalDateTime.now().plusDays(5));
        borrowingRequestRepository.save(borrowing);

        mockMvc.perform(
                patch(
                        "/api/borrowings/{borrowingId}/return",
                        borrowing.getId()
                )
                .with(userJwt(anotherUser))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
                .value("User cannot return this borrowing"));
    }

    @Test
    void user_shouldNotReturnPendingBorrowing()
            throws Exception {

        User user = createUser();
        Resource resource = createResource();

        BorrowingRequest borrowing = createBorrowing(
                user,
                resource,
                BorrowingStatus.PENDING
        );

        mockMvc.perform(
                patch(
                        "/api/borrowings/{borrowingId}/return",
                        borrowing.getId()
                )
                .with(userJwt(user))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
                .value("Only active borrowings can be returned"));
    }

    @Test
    void returningOverdueBorrowing_shouldCalculateLateFee()
            throws Exception {

        User user = createUser();
        Resource resource = createResource();

        BorrowingRequest borrowing = createBorrowing(
                user,
                resource,
                BorrowingStatus.ACTIVE
        );

        borrowing.setActivatedAt(LocalDateTime.now().minusDays(12));
        borrowing.setDueAt(LocalDateTime.now().minusDays(2));
        borrowing.setDailyLateFeeSnapshot(
                new BigDecimal("50.00")
        );

        borrowingRequestRepository.save(borrowing);

        mockMvc.perform(
                patch(
                        "/api/borrowings/{borrowingId}/return",
                        borrowing.getId()
                )
                .with(userJwt(user))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("RETURNED"))
        .andExpect(jsonPath("$.returnedAt").isNotEmpty())
        .andExpect(jsonPath("$.lateFeeAmount").value(100.00));

        BorrowingRequest returned =
                borrowingRequestRepository.findById(borrowing.getId())
                        .orElseThrow();

        assertNotNull(returned.getReturnedAt());

        assertEquals(
                new BigDecimal("100.00"),
                returned.getLateFeeAmount()
        );
    }

    @Test
    void returningOverdueBorrowing_shouldUseFeeSnapshot()
            throws Exception {

        User user = createUser();
        Resource resource = createResource();

        BorrowingRequest borrowing = createBorrowing(
                user,
                resource,
                BorrowingStatus.ACTIVE
        );

        borrowing.setDueAt(LocalDateTime.now().minusDays(2));
        borrowing.setDailyLateFeeSnapshot(
                new BigDecimal("25.00")
        );

        borrowingRequestRepository.save(borrowing);

        withSettings(
                settings -> settings.setDailyLateFee(
                        new BigDecimal("100.00")
                ),
                () -> {
                    mockMvc.perform(
                            patch(
                                    "/api/borrowings/{borrowingId}/return",
                                    borrowing.getId()
                            )
                            .with(userJwt(user))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("RETURNED"))
                    .andExpect(jsonPath("$.lateFeeAmount").value(50.00));
                }
        );
    }

    // =========================================================
    // USER BORROWING HISTORY
    // =========================================================

    @Test
    void user_shouldGetOwnBorrowings()
            throws Exception {

        User user = createUser();

        Resource resource1 = createResource();
        Resource resource2 = createResource();

        createBorrowing(user, resource1, BorrowingStatus.PENDING);
        createBorrowing(user, resource2, BorrowingStatus.ACTIVE);

        mockMvc.perform(
                get("/api/borrowings/my")
                        .with(userJwt(user))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].userId").value(user.getId()))
        .andExpect(jsonPath("$[1].userId").value(user.getId()));
    }

    @Test
    void user_shouldOnlyGetOwnBorrowings()
            throws Exception {

        User user = createUser();
        User anotherUser = createUser();

        Resource resource1 = createResource();
        Resource resource2 = createResource();

        createBorrowing(user, resource1, BorrowingStatus.ACTIVE);
        createBorrowing(anotherUser, resource2, BorrowingStatus.ACTIVE);

        mockMvc.perform(
                get("/api/borrowings/my")
                        .with(userJwt(user))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].userId").value(user.getId()))
        .andExpect(jsonPath("$[0].resourceId").value(resource1.getId()));
    }

    @Test
    void unauthenticatedUser_shouldNotGetBorrowings()
            throws Exception {

        mockMvc.perform(
                get("/api/borrowings/my")
        )
        .andExpect(status().isUnauthorized());
    }

    // =========================================================
    // ADMIN BORROWING HISTORY
    // =========================================================

    @Test
    void admin_shouldGetPendingBorrowings()
            throws Exception {

        User user = createUser();
        User admin = createAdmin();

        Resource resource1 = createResource();
        Resource resource2 = createResource();

        BorrowingRequest borrowing1 = createBorrowing(
                user,
                resource1,
                BorrowingStatus.PENDING
        );

        BorrowingRequest borrowing2 = createBorrowing(
                user,
                resource2,
                BorrowingStatus.PENDING
        );

        mockMvc.perform(
                get("/api/admin/borrowings/pending")
                        .with(adminJwt(admin))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$[?(@.id == %d)]".formatted(borrowing1.getId())
        ).isNotEmpty())
        .andExpect(jsonPath(
                "$[?(@.id == %d)]".formatted(borrowing2.getId())
        ).isNotEmpty());
    }

    @Test
    void user_shouldNotGetPendingBorrowings()
            throws Exception {

        User user = createUser();

        mockMvc.perform(
                get("/api/admin/borrowings/pending")
                        .with(userJwt(user))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUser_shouldNotGetPendingBorrowings()
            throws Exception {

        mockMvc.perform(
                get("/api/admin/borrowings/pending")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_shouldGetAllBorrowings()
            throws Exception {

        User user = createUser();
        User admin = createAdmin();

        Resource resource1 = createResource();
        Resource resource2 = createResource();

        BorrowingRequest borrowing1 = createBorrowing(
                user,
                resource1,
                BorrowingStatus.PENDING
        );

        BorrowingRequest borrowing2 = createBorrowing(
                user,
                resource2,
                BorrowingStatus.ACTIVE
        );

        mockMvc.perform(
                get("/api/admin/borrowings")
                        .with(adminJwt(admin))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$[?(@.id == %d)]".formatted(borrowing1.getId())
        ).isNotEmpty())
        .andExpect(jsonPath(
                "$[?(@.id == %d)]".formatted(borrowing2.getId())
        ).isNotEmpty());
    }

    @Test
    void user_shouldNotGetAllBorrowings()
            throws Exception {

        User user = createUser();

        mockMvc.perform(
                get("/api/admin/borrowings")
                        .with(userJwt(user))
        )
        .andExpect(status().isForbidden());
    }

    // =========================================================
    // TEST DATA HELPERS
    // =========================================================

    private User createUser() {

        User user = new User();

        user.setFullName("Test User " + System.nanoTime());
        user.setEmail("user-" + System.nanoTime() + "@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setMobileNumber("9876543210");
        user.setRole(UserRole.USER);
        user.setAccountStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }

    private User createAdmin() {

        User user = new User();

        user.setFullName("Test Admin " + System.nanoTime());
        user.setEmail("admin-" + System.nanoTime() + "@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setMobileNumber("9876543210");
        user.setRole(UserRole.ADMIN);
        user.setAccountStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }

    private Resource createResource() {

        ResourceCategory category = new ResourceCategory();

        category.setCategory("Category-" + System.nanoTime());
        category.setSubCategory("SubCategory-" + System.nanoTime());

        category = categoryRepository.save(category);

        Resource resource = new Resource();

        resource.setName("Resource-" + System.nanoTime());
        resource.setManufacturer("Test Manufacturer");
        resource.setPurchasePrice(new BigDecimal("50000.00"));
        resource.setDescription("Test resource");
        resource.setStatus(ResourceStatus.AVAILABLE);
        resource.setCategory(category);

        return resourceRepository.save(resource);
    }

    private BorrowingRequest createBorrowing(
            User user,
            Resource resource,
            BorrowingStatus borrowingStatus
    ) {
        BorrowingRequest borrowing = new BorrowingRequest();

        borrowing.setUser(user);
        borrowing.setResource(resource);
        borrowing.setStatus(borrowingStatus);
        borrowing.setLateFeeAmount(BigDecimal.ZERO);
        borrowing.setDailyLateFeeSnapshot(new BigDecimal("50.00"));
        borrowing.setBorrowingDurationDaysSnapshot(10);

        if (borrowingStatus == BorrowingStatus.ACTIVE
                || borrowingStatus == BorrowingStatus.RETURNED) {

            LocalDateTime activatedAt = LocalDateTime.now().minusDays(1);

            borrowing.setActivatedAt(activatedAt);
            borrowing.setDueAt(activatedAt.plusDays(10));
        }

        if (borrowingStatus == BorrowingStatus.RETURNED) {
            borrowing.setReturnedAt(LocalDateTime.now());
        }

        return borrowingRequestRepository.save(borrowing);
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

    private void withSettings(
            Consumer<OrganizationSettings> update,
            ThrowingRunnable action
    ) throws Exception {

        OrganizationSettings original = settingsRepository
                .findById((short) 1)
                .orElseThrow();

        String originalOrganizationName = original.getOrganizationName();
        String originalResourceLabel = original.getResourceLabel();
        Integer originalMaxActiveBorrowings =
                original.getMaxActiveBorrowings();
        Integer originalBorrowingDurationDays =
                original.getBorrowingDurationDays();
        BigDecimal originalDailyLateFee = original.getDailyLateFee();
        boolean originalBorrowingApprovalRequired =
                original.isBorrowingApprovalRequired();
        boolean originalAccountApprovalRequired =
                original.isAccountApprovalRequired();
        boolean originalEmailNotificationsEnabled =
                original.isEmailNotificationsEnabled();

        try {
            update.accept(original);
            settingsRepository.saveAndFlush(original);

            action.run();

        } finally {
            OrganizationSettings current = settingsRepository
                    .findById((short) 1)
                    .orElseThrow();

            current.setOrganizationName(originalOrganizationName);
            current.setResourceLabel(originalResourceLabel);
            current.setMaxActiveBorrowings(originalMaxActiveBorrowings);
            current.setBorrowingDurationDays(originalBorrowingDurationDays);
            current.setDailyLateFee(originalDailyLateFee);
            current.setBorrowingApprovalRequired(
                    originalBorrowingApprovalRequired
            );
            current.setAccountApprovalRequired(
                    originalAccountApprovalRequired
            );
            current.setEmailNotificationsEnabled(
                    originalEmailNotificationsEnabled
            );

            settingsRepository.saveAndFlush(current);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
