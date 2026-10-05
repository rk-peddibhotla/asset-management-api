
package com.ravikanth.asset_management.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Count the user's currently active orders
    // APPROVED and ACTIVE orders both count toward the limit
    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.user.id = :userId
              AND o.status IN (
                  com.ravikanth.asset_management.order.OrderStatus.APPROVED,
                  com.ravikanth.asset_management.order.OrderStatus.ACTIVE
              )
            """)
    long countActiveOrdersByUserId(Long userId);

    // Find one order and load its user and asset
    @Query("""
            SELECT o
            FROM Order o
            JOIN FETCH o.user
            JOIN FETCH o.asset
            WHERE o.id = :orderId
            """)
    Optional<Order> findByIdWithDetails(Long orderId);

    // Get all orders for a specific user,
    // newest orders first
    @Query("""
            SELECT o
            FROM Order o
            JOIN FETCH o.user
            JOIN FETCH o.asset
            WHERE o.user.id = :userId
            ORDER BY o.createdAt DESC
            """)
    List<Order> findAllByUserIdWithDetails(Long userId);

    // Get all orders waiting for admin approval,
    // newest orders first
    @Query("""
            SELECT o
            FROM Order o
            JOIN FETCH o.user
            JOIN FETCH o.asset
            WHERE o.status = com.ravikanth.asset_management.order.OrderStatus.PENDING
            ORDER BY o.createdAt DESC
            """)
    List<Order> findAllPendingOrdersWithDetails();
}
