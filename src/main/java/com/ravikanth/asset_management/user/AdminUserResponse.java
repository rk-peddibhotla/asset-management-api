
package com.ravikanth.asset_management.user;

public class AdminUserResponse {

    // Data that we want to send to the frontend
    private Long id;
    private String fullName;
    private String email;
    private String mobileNumber;
    private UserRole role;
    private AccountStatus accountStatus;

    // Create an API response from the given values
    public AdminUserResponse(
            Long id,
            String fullName,
            String email,
            String mobileNumber,
            UserRole role,
            AccountStatus accountStatus
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.role = role;
        this.accountStatus = accountStatus;
    }

    // Convert a User object into an AdminUserResponse object
    public static AdminUserResponse from(User user) {

        return new AdminUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole(),
                user.getAccountStatus()
        );
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }
}

