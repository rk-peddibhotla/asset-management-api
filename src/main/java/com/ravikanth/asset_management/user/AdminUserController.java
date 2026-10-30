
package com.ravikanth.asset_management.user;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // Get all registered users
    @GetMapping
    public List<AdminUserResponse> getAllUsers() {

        return userService.getAllUsers()
                .stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    // Get all users waiting for admin approval
    @GetMapping("/pending")
    public List<AdminUserResponse> getPendingUsers() {

        return userService.getPendingUsers()
                .stream()
                .map(AdminUserResponse::from)
                .toList();
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
