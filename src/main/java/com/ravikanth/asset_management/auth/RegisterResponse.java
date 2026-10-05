
package com.ravikanth.asset_management.auth;

import com.ravikanth.asset_management.user.AccountStatus;
import com.ravikanth.asset_management.user.User;
import com.ravikanth.asset_management.user.UserRole;

// Data returned to the frontend after user registration
public class RegisterResponse {

    private Long id;
    private String fullName;
    private String email;
    private String mobileNumber;
    private UserRole role;
    private AccountStatus accountStatus;

    // Create an API response using the given values
    public RegisterResponse(
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

    // Convert a User object into a RegisterResponse
    public static RegisterResponse from(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole(),
                user.getAccountStatus()
        );
    }

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

