
package com.ravikanth.asset_management.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by their email address
    Optional<User> findByEmail(String email);

    // Find users with a specific account status,
    // showing the newest users first
    List<User> findByAccountStatusOrderByCreatedAtDesc(
            AccountStatus accountStatus
    );
}

