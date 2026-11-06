package com.ravikanth.asset_management.borrowing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Data returned to the frontend for a borrowing
public class BorrowingResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long resourceId;
    private String resourceName;
    private BorrowingStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime dueAt;
    private LocalDateTime returnedAt;
    private BigDecimal lateFeeAmount;

    public BorrowingResponse(
            Long id,
            Long userId,
            String userName,
            String userEmail,
            Long resourceId,
            String resourceName,
            BorrowingStatus status,
            LocalDateTime requestedAt,
            LocalDateTime activatedAt,
            LocalDateTime dueAt,
            LocalDateTime returnedAt,
            BigDecimal lateFeeAmount
    ) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.status = status;
        this.requestedAt = requestedAt;
        this.activatedAt = activatedAt;
        this.dueAt = dueAt;
        this.returnedAt = returnedAt;
        this.lateFeeAmount = lateFeeAmount;
    }

    // Convert a BorrowingRequest entity into a BorrowingResponse
    public static BorrowingResponse from(BorrowingRequest borrowing) {

        return new BorrowingResponse(
                borrowing.getId(),
                borrowing.getUser().getId(),
                borrowing.getUser().getFullName(),
                borrowing.getUser().getEmail(),
                borrowing.getResource().getId(),
                borrowing.getResource().getName(),
                borrowing.getStatus(),
                borrowing.getRequestedAt(),
                borrowing.getActivatedAt(),
                borrowing.getDueAt(),
                borrowing.getReturnedAt(),
                borrowing.getLateFeeAmount()
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

    public Long getResourceId() {
        return resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public BorrowingStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public BigDecimal getLateFeeAmount() {
        return lateFeeAmount;
    }
}