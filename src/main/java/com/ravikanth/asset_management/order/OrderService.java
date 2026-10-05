
package com.ravikanth.asset_management.order;

import com.ravikanth.asset_management.asset.Asset;
import com.ravikanth.asset_management.asset.AssetRepository;
import com.ravikanth.asset_management.user.User;
import com.ravikanth.asset_management.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    // Business rules for asset orders
    private static final int MAX_ACTIVE_ORDERS = 3;
    private static final int LOAN_PERIOD_DAYS = 10;
    private static final BigDecimal DAILY_FINE =
            new BigDecimal("50.00");

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            AssetRepository assetRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
    }

    // Create a new asset order
    @Transactional
    public Order createOrder(Long userId, Long assetId) {

        // Find the user
        Optional<User> userResult =
                userRepository.findById(userId);

        if (userResult.isEmpty()) {
            throw new IllegalArgumentException(
                    "User not found"
            );
        }

        User user = userResult.get();

        // Find the requested asset
        Optional<Asset> assetResult =
                assetRepository.findById(assetId);

        if (assetResult.isEmpty()) {
            throw new IllegalArgumentException(
                    "Asset not found"
            );
        }

        Asset asset = assetResult.get();

        // Check the user's current active-order count
        long activeOrderCount =
                orderRepository.countActiveOrdersByUserId(userId);

        if (activeOrderCount >= MAX_ACTIVE_ORDERS) {
            throw new IllegalArgumentException(
                    "User cannot have more than 3 active orders"
            );
        }

        // Create the order
        Order order = new Order();

        order.setUser(user);
        order.setAsset(asset);
        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    // Get all orders belonging to a user
    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(Long userId) {
        return orderRepository.findAllByUserIdWithDetails(userId);
    }

    // Approve a pending order
    @Transactional
    public Order approveOrder(Long orderId) {

        Optional<Order> orderResult =
                orderRepository.findByIdWithDetails(orderId);

        if (orderResult.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order not found"
            );
        }

        Order order = orderResult.get();

        // Only pending orders can be approved
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending orders can be approved"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        // Start the loan period
        order.setStatus(OrderStatus.ACTIVE);
        order.setApprovedAt(now);
        order.setDueAt(
                now.plusDays(LOAN_PERIOD_DAYS)
        );

        return orderRepository.save(order);
    }

    // Reject a pending order
    @Transactional
    public Order rejectOrder(Long orderId) {

        Optional<Order> orderResult =
                orderRepository.findByIdWithDetails(orderId);

        if (orderResult.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order not found"
            );
        }

        Order order = orderResult.get();

        // Only pending orders can be rejected
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending orders can be rejected"
            );
        }

        order.setStatus(OrderStatus.REJECTED);

        return orderRepository.save(order);
    }

    // Return an active order
    @Transactional
    public Order returnOrder(Long orderId, Long userId) {

        Optional<Order> orderResult =
                orderRepository.findByIdWithDetails(orderId);

        if (orderResult.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order not found"
            );
        }

        Order order = orderResult.get();

        // Make sure the logged-in user owns this order
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "User cannot return this order"
            );
        }

        // Only active orders can be returned
        if (order.getStatus() != OrderStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Only active orders can be returned"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        order.setStatus(OrderStatus.RETURNED);
        order.setReturnedAt(now);

        // Calculate a fine if the order is returned late
        if (now.isAfter(order.getDueAt())) {

            long overdueDays =
                    ChronoUnit.DAYS.between(
                            order.getDueAt().toLocalDate(),
                            now.toLocalDate()
                    );

            BigDecimal fine =
                    DAILY_FINE.multiply(
                            BigDecimal.valueOf(overdueDays)
                    );

            order.setFineAmount(fine);

        } else {
            // No fine if the asset is returned on time
            order.setFineAmount(BigDecimal.ZERO);
        }

        return orderRepository.save(order);
    }

    // Get all orders waiting for admin approval
    @Transactional(readOnly = true)
    public List<Order> getPendingOrders() {
        return orderRepository.findAllPendingOrdersWithDetails();
    }
}

