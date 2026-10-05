
package com.ravikanth.asset_management.order;

import com.ravikanth.asset_management.asset.Asset;
import com.ravikanth.asset_management.asset.AssetCategory;
import com.ravikanth.asset_management.asset.AssetCategoryRepository;
import com.ravikanth.asset_management.asset.AssetRepository;
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
import java.time.LocalDateTime;

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
class OrderControllerTest {

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

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void authenticatedStudent_shouldCreateOrder() throws Exception {

        User student = createStudent();

        Asset asset = createAsset();

        mockMvc.perform(
                post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "assetId": %d
                                }
                                """.formatted(asset.getId()))
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
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.userId")
                .value(student.getId()))
        .andExpect(jsonPath("$.assetId")
                .value(asset.getId()))
        .andExpect(jsonPath("$.assetName")
                .value(asset.getName()))
        .andExpect(jsonPath("$.status")
                .value("PENDING"))
        .andExpect(jsonPath("$.requestedAt").isNotEmpty())
        .andExpect(jsonPath("$.approvedAt").doesNotExist())
        .andExpect(jsonPath("$.dueAt").doesNotExist())
        .andExpect(jsonPath("$.returnedAt").doesNotExist())
        .andExpect(jsonPath("$.fineAmount").value(0));
    }

    @Test
    void unauthenticatedUser_shouldNotCreateOrder()
            throws Exception {

        Asset asset = createAsset();

        mockMvc.perform(
                post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "assetId": %d
                                }
                                """.formatted(asset.getId()))
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedStudent_shouldNotCreateOrderForMissingAsset()
            throws Exception {

        User student = createStudent();

        mockMvc.perform(
                post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "assetId": 999999999
                                }
                                """)
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
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("Asset not found"));
    }

    @Test
    void studentWithThreeActiveOrders_shouldNotCreateFourthOrder()
            throws Exception {

        User student = createStudent();

        Asset asset1 = createAsset();
        Asset asset2 = createAsset();
        Asset asset3 = createAsset();
        Asset asset4 = createAsset();

        createOrder(student, asset1, OrderStatus.ACTIVE);
        createOrder(student, asset2, OrderStatus.ACTIVE);
        createOrder(student, asset3, OrderStatus.ACTIVE);

        mockMvc.perform(
                post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "assetId": %d
                                }
                                """.formatted(asset4.getId()))
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
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("User cannot have more than 3 active orders"));
    }

    @Test
    void admin_shouldApprovePendingOrder() throws Exception {

        User student = createStudent();
        User admin = createAdmin();

        Asset asset = createAsset();

        Order order = createOrder(
                student,
                asset,
                OrderStatus.PENDING
        );

        mockMvc.perform(
                patch("/api/admin/orders/{orderId}/approve", order.getId())
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
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value(order.getId()))
        .andExpect(jsonPath("$.userId")
                .value(student.getId()))
        .andExpect(jsonPath("$.assetId")
                .value(asset.getId()))
        .andExpect(jsonPath("$.status")
                .value("ACTIVE"))
        .andExpect(jsonPath("$.requestedAt").isNotEmpty())
        .andExpect(jsonPath("$.approvedAt").isNotEmpty())
        .andExpect(jsonPath("$.dueAt").isNotEmpty())
        .andExpect(jsonPath("$.returnedAt").doesNotExist())
        .andExpect(jsonPath("$.fineAmount").value(0));
    }

    @Test
    void student_shouldNotApproveOrder() throws Exception {

        User student = createStudent();

        Asset asset = createAsset();

        Order order = createOrder(
                student,
                asset,
                OrderStatus.PENDING
        );

        mockMvc.perform(
                patch("/api/admin/orders/{orderId}/approve", order.getId())
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
    void admin_shouldNotApproveAlreadyActiveOrder()
            throws Exception {

        User student = createStudent();
        User admin = createAdmin();

        Asset asset = createAsset();

        Order order = createOrder(
                student,
                asset,
                OrderStatus.ACTIVE
        );

        mockMvc.perform(
                patch("/api/admin/orders/{orderId}/approve", order.getId())
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
                .value("Only pending orders can be approved"));
    }

    @Test
    void adminApproval_shouldSetDueDate()
            throws Exception {

        User student = createStudent();
        User admin = createAdmin();

        Asset asset = createAsset();

        Order order = createOrder(
                student,
                asset,
                OrderStatus.PENDING
        );

        mockMvc.perform(
                patch("/api/admin/orders/{orderId}/approve", order.getId())
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
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status")
                .value("ACTIVE"));

        Order approvedOrder =
                orderRepository.findById(order.getId())
                        .orElseThrow();

        assertNotNull(approvedOrder.getApprovedAt());
        assertNotNull(approvedOrder.getDueAt());

        assertEquals(
                approvedOrder.getApprovedAt().plusDays(10),
                approvedOrder.getDueAt()
        );
    }

    @Test
    void admin_shouldRejectPendingOrder() throws Exception {

        User student = createStudent();
        User admin = createAdmin();

        Asset asset = createAsset();

        Order order = createOrder(
                student,
                asset,
                OrderStatus.PENDING
        );

        mockMvc.perform(
                patch("/api/admin/orders/{orderId}/reject", order.getId())
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
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
                .value(order.getId()))
        .andExpect(jsonPath("$.userId")
                .value(student.getId()))
        .andExpect(jsonPath("$.assetId")
                .value(asset.getId()))
        .andExpect(jsonPath("$.status")
                .value("REJECTED"));
    }

    @Test
    void student_shouldNotRejectOrder() throws Exception {

        User student = createStudent();

        Asset asset = createAsset();

        Order order = createOrder(
                student,
                asset,
                OrderStatus.PENDING
        );

        mockMvc.perform(
                patch("/api/admin/orders/{orderId}/reject", order.getId())
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
    void admin_shouldNotRejectAlreadyActiveOrder()
            throws Exception {

        User student = createStudent();
        User admin = createAdmin();

        Asset asset = createAsset();

        Order order = createOrder(
                student,
                asset,
                OrderStatus.ACTIVE
        );

        mockMvc.perform(
                patch("/api/admin/orders/{orderId}/reject", order.getId())
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
                .value("Only pending orders can be rejected"));
    }

    @Test
    void student_shouldReturnOwnActiveOrder() throws Exception {

        User student = createStudent();

        Asset asset = createAsset();

        Order order = createOrder(
                student,
                asset,
                OrderStatus.ACTIVE
        );

        order.setApprovedAt(LocalDateTime.now().minusDays(5));
        order.setDueAt(LocalDateTime.now().plusDays(5));
        orderRepository.save(order);

        mockMvc.perform(
                patch("/api/orders/{orderId}/return", order.getId())
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
        .andExpect(jsonPath("$.id")
                .value(order.getId()))
        .andExpect(jsonPath("$.userId")
                .value(student.getId()))
        .andExpect(jsonPath("$.assetId")
                .value(asset.getId()))
        .andExpect(jsonPath("$.status")
                .value("RETURNED"))
        .andExpect(jsonPath("$.requestedAt").isNotEmpty())
        .andExpect(jsonPath("$.approvedAt").isNotEmpty())
        .andExpect(jsonPath("$.dueAt").isNotEmpty())
        .andExpect(jsonPath("$.returnedAt").isNotEmpty())
        .andExpect(jsonPath("$.fineAmount").value(0));

        Order returnedOrder =
                orderRepository.findById(order.getId())
                        .orElseThrow();

        assertNotNull(returnedOrder.getReturnedAt());

        assertEquals(
                0,
                returnedOrder.getFineAmount().compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void student_shouldNotReturnAnotherUsersOrder()
            throws Exception {

        User owner = createStudent();
        User anotherStudent = createStudent();

        Asset asset = createAsset();

        Order order = createOrder(
                owner,
                asset,
                OrderStatus.ACTIVE
        );

        order.setDueAt(LocalDateTime.now().plusDays(5));
        orderRepository.save(order);

        mockMvc.perform(
                patch("/api/orders/{orderId}/return", order.getId())
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .subject(anotherStudent.getId().toString())
                                        .claim("email", anotherStudent.getEmail())
                                        .claim("role", "STUDENT")
                                )
                                .authorities(
                                        new org.springframework.security.core.authority
                                                .SimpleGrantedAuthority("ROLE_STUDENT")
                                )
                        )
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("User cannot return this order"));
    }

    @Test
    void student_shouldNotReturnPendingOrder()
            throws Exception {

        User student = createStudent();

        Asset asset = createAsset();

        Order order = createOrder(
                student,
                asset,
                OrderStatus.PENDING
        );

        mockMvc.perform(
                patch("/api/orders/{orderId}/return", order.getId())
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
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message")
                .value("Only active orders can be returned"));
    }

    @Test
    void returningOverdueOrder_shouldCalculateFine()
            throws Exception {

        User student = createStudent();

        Asset asset = createAsset();

        Order order = createOrder(
                student,
                asset,
                OrderStatus.ACTIVE
        );

        order.setApprovedAt(LocalDateTime.now().minusDays(12));
        order.setDueAt(
                LocalDateTime.now().minusDays(2)
        );

        orderRepository.save(order);

        mockMvc.perform(
                patch("/api/orders/{orderId}/return", order.getId())
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
        .andExpect(jsonPath("$.status")
                .value("RETURNED"))
        .andExpect(jsonPath("$.requestedAt").isNotEmpty())
        .andExpect(jsonPath("$.approvedAt").isNotEmpty())
        .andExpect(jsonPath("$.dueAt").isNotEmpty())
        .andExpect(jsonPath("$.returnedAt").isNotEmpty())
        .andExpect(jsonPath("$.fineAmount").value(100.00));

        Order returnedOrder =
                orderRepository.findById(order.getId())
                        .orElseThrow();

        assertNotNull(returnedOrder.getReturnedAt());

        assertEquals(
                new BigDecimal("100.00"),
                returnedOrder.getFineAmount()
        );
    }

    @Test
    void student_shouldGetOwnOrders() throws Exception {

        User student = createStudent();

        Asset asset1 = createAsset();
        Asset asset2 = createAsset();

        createOrder(
                student,
                asset1,
                OrderStatus.PENDING
        );

        createOrder(
                student,
                asset2,
                OrderStatus.ACTIVE
        );

        mockMvc.perform(
                get("/api/orders/my")
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
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].userId")
                .value(student.getId()))
        .andExpect(jsonPath("$[1].userId")
                .value(student.getId()));
    }

    @Test
    void student_shouldOnlyGetOwnOrders() throws Exception {

        User student = createStudent();
        User anotherStudent = createStudent();

        Asset studentAsset = createAsset();
        Asset anotherStudentAsset = createAsset();

        createOrder(
                student,
                studentAsset,
                OrderStatus.ACTIVE
        );

        createOrder(
                anotherStudent,
                anotherStudentAsset,
                OrderStatus.ACTIVE
        );

        mockMvc.perform(
                get("/api/orders/my")
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
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].userId")
                .value(student.getId()))
        .andExpect(jsonPath("$[0].assetId")
                .value(studentAsset.getId()));
    }

    @Test
    void unauthenticatedUser_shouldNotGetOrders() throws Exception {

        mockMvc.perform(
                get("/api/orders/my")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_shouldGetPendingOrders() throws Exception {

        User student = createStudent();
        User admin = createAdmin();

        Asset asset1 = createAsset();
        Asset asset2 = createAsset();

        Order order1 = createOrder(
                student,
                asset1,
                OrderStatus.PENDING
        );

        Order order2 = createOrder(
                student,
                asset2,
                OrderStatus.PENDING
        );

        mockMvc.perform(
                get("/api/admin/orders/pending")
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
        .andExpect(status().isOk())
        .andExpect(jsonPath(
                "$[?(@.id == %d)]".formatted(order1.getId())
        ).isNotEmpty())
        .andExpect(jsonPath(
                "$[?(@.id == %d)]".formatted(order2.getId())
        ).isNotEmpty());
    }

    @Test
    void student_shouldNotGetPendingOrders() throws Exception {

        User student = createStudent();

        mockMvc.perform(
                get("/api/admin/orders/pending")
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
    void unauthenticatedUser_shouldNotGetPendingOrders()
            throws Exception {

        mockMvc.perform(
                get("/api/admin/orders/pending")
        )
        .andExpect(status().isUnauthorized());
    }

    private User createStudent() {

        User user = new User();

        user.setFullName(
                "Test Student " + System.nanoTime()
        );

        user.setEmail(
                "student-" + System.nanoTime()
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

    private Asset createAsset() {

        AssetCategory category = new AssetCategory();

        category.setCategory(
                "Category-" + System.nanoTime()
        );

        category.setSubCategory(
                "SubCategory-" + System.nanoTime()
        );

        category = categoryRepository.save(category);

        Asset asset = new Asset();

        asset.setName(
                "Laptop-" + System.nanoTime()
        );

        asset.setManufacturer("Test Manufacturer");
        asset.setPrice(new BigDecimal("50000.00"));
        asset.setCategory(category);

        return assetRepository.save(asset);
    }

    private Order createOrder(
            User user,
            Asset asset,
            OrderStatus status
    ) {

        Order order = new Order();

        order.setUser(user);
        order.setAsset(asset);
        order.setStatus(status);

        return orderRepository.save(order);
    }
}

