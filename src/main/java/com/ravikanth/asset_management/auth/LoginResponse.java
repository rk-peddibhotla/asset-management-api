
package com.ravikanth.asset_management.auth;

// Data returned to the frontend after successful login
public class LoginResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String role;

    // Create a login response using the given values
    public LoginResponse(
            String token,
            String tokenType,
            Long userId,
            String email,
            String role
    ) {
        this.token = token;
        this.tokenType = tokenType;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}

