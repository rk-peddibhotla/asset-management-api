
package com.ravikanth.asset_management.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Register a new student
    @Transactional
    public User registerUser(
            String fullName,
            String email,
            String password,
            String mobileNumber
    ) {

        // Check whether the email is already registered
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            throw new IllegalArgumentException(
                    "Email already registered"
            );
        }

        // Create a new user
        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);

        // Store the encoded password, not the original password
        String encodedPassword = passwordEncoder.encode(password);
        user.setPasswordHash(encodedPassword);

        user.setMobileNumber(mobileNumber);

        // Every new registered user starts as a student
        user.setRole(UserRole.STUDENT);

        // An admin must approve the account
        user.setAccountStatus(AccountStatus.UNAPPROVED);

        return userRepository.save(user);
    }

    // Get all users waiting for admin approval
    @Transactional(readOnly = true)
    public List<User> getPendingUsers() {

        return userRepository.findByAccountStatusOrderByCreatedAtDesc(
                AccountStatus.UNAPPROVED
        );
    }

    // Approve a user
    @Transactional
    public User approveUser(Long userId) {

        // Find the user
        Optional<User> userResult = userRepository.findById(userId);

        if (userResult.isEmpty()) {
            throw new IllegalArgumentException(
                    "User not found"
            );
        }

        User user = userResult.get();

        // Only unapproved users can be approved
        if (user.getAccountStatus() != AccountStatus.UNAPPROVED) {
            throw new IllegalArgumentException(
                    "Only unapproved users can be approved"
            );
        }

        // Change the account status
        user.setAccountStatus(AccountStatus.ACTIVE);

        // Save the updated user
        return userRepository.save(user);
    }

    // Block a user
    @Transactional
    public User blockUser(Long userId) {

        // Find the user
        Optional<User> userResult = userRepository.findById(userId);

        if (userResult.isEmpty()) {
            throw new IllegalArgumentException(
                    "User not found"
            );
        }

        User user = userResult.get();

        // Do not block a user who is already blocked
        if (user.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new IllegalArgumentException(
                    "User is already blocked"
            );
        }

        // Change the account status
        user.setAccountStatus(AccountStatus.BLOCKED);

        // Save the updated user
        return userRepository.save(user);
    }
}

