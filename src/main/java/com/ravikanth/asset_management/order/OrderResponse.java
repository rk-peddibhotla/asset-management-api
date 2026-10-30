package com.ravikanth.asset_management.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Data returned to the frontend for an order
public class OrderResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long assetId;
    private String assetName;
    private OrderStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime dueAt;
    private LocalDateTime returnedAt;
    private BigDecimal fineAmount;

    public OrderResponse(
            Long id,
            Long userId,
            String userName,
            String userEmail,
            Long assetId,
            String assetName,
            OrderStatus status,
            LocalDateTime requestedAt,
            LocalDateTime approvedAt,
            LocalDateTime dueAt,
            LocalDateTime returnedAt,
            BigDecimal fineAmount
    ) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.assetId = assetId;
        this.assetName = assetName;
        this.status = status;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.dueAt = dueAt;
        this.returnedAt = returnedAt;
        this.fineAmount = fineAmount;
    }

    // Convert an Order entity into an OrderResponse
    public static OrderResponse from(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getFullName(),
                order.getUser().getEmail(),
                order.getAsset().getId(),
                order.getAsset().getName(),
                order.getStatus(),
                order.getRequestedAt(),
                order.getApprovedAt(),
                order.getDueAt(),
                order.getReturnedAt(),
                order.getFineAmount()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }
}