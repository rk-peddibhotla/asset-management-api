package com.ravikanth.asset_management.organization;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization_settings")
public class OrganizationSettings {

    @Id
    @Column(nullable = false)
    private Short id = 1;

    @Column(name = "organization_name", nullable = false, length = 150)
    private String organizationName;

    @Column(name = "resource_label", nullable = false, length = 50)
    private String resourceLabel;

    @Column(name = "max_active_borrowings", nullable = false)
    private Integer maxActiveBorrowings;

    @Column(name = "borrowing_duration_days", nullable = false)
    private Integer borrowingDurationDays;

    @Column(name = "daily_late_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyLateFee;

    @Column(name = "borrowing_approval_required", nullable = false)
    private boolean borrowingApprovalRequired;

    @Column(name = "account_approval_required", nullable = false)
    private boolean accountApprovalRequired;

    @Column(name = "email_notifications_enabled", nullable = false)
    private boolean emailNotificationsEnabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Short getId() {
        return id;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getResourceLabel() {
        return resourceLabel;
    }

    public void setResourceLabel(String resourceLabel) {
        this.resourceLabel = resourceLabel;
    }

    public Integer getMaxActiveBorrowings() {
        return maxActiveBorrowings;
    }

    public void setMaxActiveBorrowings(Integer maxActiveBorrowings) {
        this.maxActiveBorrowings = maxActiveBorrowings;
    }

    public Integer getBorrowingDurationDays() {
        return borrowingDurationDays;
    }

    public void setBorrowingDurationDays(Integer borrowingDurationDays) {
        this.borrowingDurationDays = borrowingDurationDays;
    }

    public BigDecimal getDailyLateFee() {
        return dailyLateFee;
    }

    public void setDailyLateFee(BigDecimal dailyLateFee) {
        this.dailyLateFee = dailyLateFee;
    }

    public boolean isBorrowingApprovalRequired() {
        return borrowingApprovalRequired;
    }

    public void setBorrowingApprovalRequired(boolean borrowingApprovalRequired) {
        this.borrowingApprovalRequired = borrowingApprovalRequired;
    }

    public boolean isAccountApprovalRequired() {
        return accountApprovalRequired;
    }

    public void setAccountApprovalRequired(boolean accountApprovalRequired) {
        this.accountApprovalRequired = accountApprovalRequired;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}