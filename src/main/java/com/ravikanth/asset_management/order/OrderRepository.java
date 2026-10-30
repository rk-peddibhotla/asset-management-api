package com.ravikanth.asset_management.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Count the user's currently active orders
    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.user.id = :userId
              AND o.status = com.ravikanth.asset_management.order.OrderStatus.ACTIVE
            """)
    long countActiveOrdersByUserId(@Param("userId") Long userId);

    // Check whether an asset is currently borrowed
    @Query("""
            SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
            FROM Order o
            WHERE o.asset.id = :assetId
              AND o.status = com.ravikanth.asset_management.order.OrderStatus.ACTIVE
            """)
    boolean existsActiveOrderForAsset(@Param("assetId") Long assetId);

    // Find one order and load its user and asset
    @Query("""
            SELECT o
            FROM Order o
            JOIN FETCH o.user
            JOIN FETCH o.asset
            WHERE o.id = :orderId
            """)
    Optional<Order> findByIdWithDetails(
            @Param("orderId") Long orderId
    );

    // Get all orders for a specific user, newest first
    @Query("""
            SELECT o
            FROM Order o
            JOIN FETCH o.user
            JOIN FETCH o.asset
            WHERE o.user.id = :userId
            ORDER BY o.createdAt DESC
            """)
    List<Order> findAllByUserIdWithDetails(
            @Param("userId") Long userId
    );

    // Get pending orders for administrators
    @Query("""
            SELECT o
            FROM Order o
            JOIN FETCH o.user
            JOIN FETCH o.asset
            WHERE o.status = com.ravikanth.asset_management.order.OrderStatus.PENDING
            ORDER BY o.createdAt DESC
            """)
    List<Order> findAllPendingOrdersWithDetails();

    // Get complete borrowing history for administrators
    @Query("""
            SELECT o
            FROM Order o
            JOIN FETCH o.user
            JOIN FETCH o.asset
            ORDER BY o.createdAt DESC
            """)
    List<Order> findAllOrdersWithDetails();
}