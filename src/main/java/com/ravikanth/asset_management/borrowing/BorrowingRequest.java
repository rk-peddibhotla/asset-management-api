
package com.ravikanth.asset_management.borrowing;

import com.ravikanth.asset_management.resource.Resource;
import com.ravikanth.asset_management.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowing_requests")
public class BorrowingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_borrowing_requests_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "resource_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_borrowing_requests_resource")
    )
    private Resource resource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BorrowingStatus status = BorrowingStatus.PENDING;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(
            name = "late_fee_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal lateFeeAmount;

    // Daily fee rate saved when the borrowing request is created
    @Column(
            name = "daily_late_fee_snapshot",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal dailyLateFeeSnapshot;

    // Borrowing duration saved when the borrowing request is created
    @Column(
            name = "borrowing_duration_days_snapshot",
            nullable = false
    )
    private Integer borrowingDurationDaysSnapshot;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        requestedAt = now;
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = BorrowingStatus.PENDING;
        }

        if (lateFeeAmount == null) {
            lateFeeAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public BorrowingStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowingStatus status) {
        this.status = status;
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

    public BigDecimal getDailyLateFeeSnapshot() {
        return dailyLateFeeSnapshot;
    }

    public Integer getBorrowingDurationDaysSnapshot() {
        return borrowingDurationDaysSnapshot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public void setLateFeeAmount(BigDecimal lateFeeAmount) {
        this.lateFeeAmount = lateFeeAmount;
    }

    public void setDailyLateFeeSnapshot(
            BigDecimal dailyLateFeeSnapshot
    ) {
        this.dailyLateFeeSnapshot = dailyLateFeeSnapshot;
    }

    public void setBorrowingDurationDaysSnapshot(
            Integer borrowingDurationDaysSnapshot
    ) {
        this.borrowingDurationDaysSnapshot =
                borrowingDurationDaysSnapshot;
    }
}
