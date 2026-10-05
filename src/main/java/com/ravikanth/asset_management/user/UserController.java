package com.ravikanth.asset_management.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // Get information about the currently logged-in user
    @GetMapping("/me")
    public String getCurrentUser(Authentication authentication) {

        // Spring Security gives us the authenticated user's name
        String username = authentication.getName();

        return "Authenticated as: " + username;
    }
}

