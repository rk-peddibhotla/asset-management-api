
package com.ravikanth.asset_management.order;

import com.ravikanth.asset_management.asset.Asset;
import com.ravikanth.asset_management.asset.AssetRepository;
import com.ravikanth.asset_management.user.User;
import com.ravikanth.asset_management.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class OrderService {

    private static final int MAX_ACTIVE_ORDERS = 3;
    private static final int LOAN_PERIOD_DAYS = 10;

    private static final BigDecimal DAILY_FINE =
            new BigDecimal("50.00");

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final EntityManager entityManager;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            AssetRepository assetRepository,
            EntityManager entityManager
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.entityManager = entityManager;
    }

    // Create a new borrowing request
    @Transactional
    public Order createOrder(Long userId, Long assetId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Asset not found"
                        )
                );

        long activeOrderCount =
                orderRepository.countActiveOrdersByUserId(userId);

        if (activeOrderCount >= MAX_ACTIVE_ORDERS) {
            throw new IllegalArgumentException(
                    "User cannot have more than 3 active orders"
            );
        }

        Order order = new Order();

        order.setUser(user);
        order.setAsset(asset);
        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    // Get the logged-in student's order history
    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(Long userId) {
        return orderRepository.findAllByUserIdWithDetails(userId);
    }

    // Get all orders for administrator history
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllOrdersWithDetails();
    }

    // Get pending orders for administrator approval
    @Transactional(readOnly = true)
    public List<Order> getPendingOrders() {
        return orderRepository.findAllPendingOrdersWithDetails();
    }

    // Approve a pending borrowing request
    @Transactional
    public Order approveOrder(Long orderId) {

        // Read the order to identify the user and asset.
        Order initialOrder = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found"
                        )
                );

        Long userId = initialOrder.getUser().getId();
        Long assetId = initialOrder.getAsset().getId();

        // Serialize approvals involving the same student.
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        entityManager.lock(
                user,
                LockModeType.PESSIMISTIC_WRITE
        );

        // Serialize approvals involving the same asset.
        assetRepository.findByIdForUpdate(assetId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Asset not found"
                        )
                );

        // Reload the order after acquiring the locks.
        entityManager.refresh(initialOrder);

        Order order = initialOrder;

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending orders can be approved"
            );
        }

        long activeOrderCount =
                orderRepository.countActiveOrdersByUserId(userId);

        if (activeOrderCount >= MAX_ACTIVE_ORDERS) {
            throw new IllegalArgumentException(
                    "User cannot have more than 3 active orders"
            );
        }

        boolean assetBorrowed =
                orderRepository.existsActiveOrderForAsset(assetId);

        if (assetBorrowed) {
            throw new IllegalArgumentException(
                    "Asset is currently borrowed"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        order.setStatus(OrderStatus.ACTIVE);
        order.setApprovedAt(now);
        order.setDueAt(now.plusDays(LOAN_PERIOD_DAYS));

        return orderRepository.save(order);
    }

    // Reject a pending borrowing request
    @Transactional
    public Order rejectOrder(Long orderId) {

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found"
                        )
                );

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending orders can be rejected"
            );
        }

        order.setStatus(OrderStatus.REJECTED);

        return orderRepository.save(order);
    }

    // Return an active borrowing order
    @Transactional
    public Order returnOrder(Long orderId, Long userId) {

        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found"
                        )
                );

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "User cannot return this order"
            );
        }

        if (order.getStatus() != OrderStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Only active orders can be returned"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        order.setStatus(OrderStatus.RETURNED);
        order.setReturnedAt(now);

        if (now.isAfter(order.getDueAt())) {

            long overdueDays = ChronoUnit.DAYS.between(
                    order.getDueAt().toLocalDate(),
                    now.toLocalDate()
            );

            BigDecimal fine = DAILY_FINE.multiply(
                    BigDecimal.valueOf(overdueDays)
            );

            order.setFineAmount(fine);

        } else {
            order.setFineAmount(BigDecimal.ZERO);
        }

        return orderRepository.save(order);
    }
}
