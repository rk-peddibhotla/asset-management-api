
package com.ravikanth.asset_management.user;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users waiting for admin approval
    @GetMapping("/pending")
    public List<AdminUserResponse> getPendingUsers() {

        // Get users from the service
        List<User> users = userService.getPendingUsers();

        // Create a list for the API responses
        List<AdminUserResponse> responses = new ArrayList<>();

        // Convert each User into an AdminUserResponse
        for (User user : users) {
            AdminUserResponse response = AdminUserResponse.from(user);
            responses.add(response);
        }

        return responses;
    }

    // Approve a user
    @PatchMapping("/{userId}/approve")
    public AdminUserResponse approveUser(
            @PathVariable Long userId
    ) {

        User user = userService.approveUser(userId);

        return AdminUserResponse.from(user);
    }

    // Block a user
    @PatchMapping("/{userId}/block")
    public AdminUserResponse blockUser(
            @PathVariable Long userId
    ) {

        User user = userService.blockUser(userId);

        return AdminUserResponse.from(user);
    }
}

