
package com.ravikanth.asset_management.order;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Create a new asset order
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication
    ) {

        Long userId = Long.valueOf(authentication.getName());

        Order order = orderService.createOrder(
                userId,
                request.getAssetId()
        );

        return OrderResponse.from(order);
    }

    // Approve a pending order
    @PatchMapping("/admin/orders/{orderId}/approve")
    public OrderResponse approveOrder(
            @PathVariable Long orderId
    ) {

        Order order = orderService.approveOrder(orderId);

        return OrderResponse.from(order);
    }

    // Reject a pending order
    @PatchMapping("/admin/orders/{orderId}/reject")
    public OrderResponse rejectOrder(
            @PathVariable Long orderId
    ) {

        Order order = orderService.rejectOrder(orderId);

        return OrderResponse.from(order);
    }

    // Return an active order
    @PatchMapping("/orders/{orderId}/return")
    public OrderResponse returnOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        Long userId = Long.valueOf(authentication.getName());

        Order order = orderService.returnOrder(
                orderId,
                userId
        );

        return OrderResponse.from(order);
    }

    // Get orders belonging to the logged-in user
    @GetMapping("/orders/my")
    public List<OrderResponse> getMyOrders(
            Authentication authentication
    ) {

        Long userId = Long.valueOf(authentication.getName());

        return orderService.getOrdersForUser(userId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    // Get orders waiting for admin approval
    @GetMapping("/admin/orders/pending")
    public List<OrderResponse> getPendingOrders() {

        return orderService.getPendingOrders()
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    // Get complete order history for administrators
    @GetMapping("/admin/orders")
    public List<OrderResponse> getAllOrders() {

        return orderService.getAllOrders()
                .stream()
                .map(OrderResponse::from)
                .toList();
    }
}
