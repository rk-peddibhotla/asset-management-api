
package com.ravikanth.asset_management.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // Simple admin dashboard endpoint
    @GetMapping("/dashboard")
    public String getAdminDashboard() {

        return "Admin dashboard";
    }
}

