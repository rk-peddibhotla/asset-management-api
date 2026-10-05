
package com.ravikanth.asset_management.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Data received from the frontend when logging in
public class LoginRequest {

    // Email is required and must be a valid email address
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    // Password is required
    @NotBlank(message = "Password is required")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

