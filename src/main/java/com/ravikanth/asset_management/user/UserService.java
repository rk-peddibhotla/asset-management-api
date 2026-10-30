
package com.ravikanth.asset_management.user;

import com.ravikanth.asset_management.common.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // Register a new student
    @Transactional
    public User registerUser(
            String fullName,
            String email,
            String password,
            String mobileNumber
    ) {
        Optional<User> existingUser =
                userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            throw new IllegalArgumentException(
                    "Email already registered"
            );
        }

        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);

        String encodedPassword =
                passwordEncoder.encode(password);

        user.setPasswordHash(encodedPassword);
        user.setMobileNumber(mobileNumber);
        user.setRole(UserRole.STUDENT);
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

    // Get all registered users for administrators
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Approve a user and send an approval email
    @Transactional
    public User approveUser(Long userId) {

        Optional<User> userResult =
                userRepository.findById(userId);

        if (userResult.isEmpty()) {
            throw new IllegalArgumentException(
                    "User not found"
            );
        }

        User user = userResult.get();

        if (user.getAccountStatus() != AccountStatus.UNAPPROVED) {
            throw new IllegalArgumentException(
                    "Only unapproved users can be approved"
            );
        }

        user.setAccountStatus(AccountStatus.ACTIVE);

        User approvedUser = userRepository.save(user);

        // Email failure should not stop account approval
        try {
            String subject =
                    "Your Asset Management account has been approved";

            String body =
                    "Hello " + approvedUser.getFullName() + ",\n\n"
                    + "Your Asset Management System account has been "
                    + "approved by the administrator.\n\n"
                    + "You can now log in using your registered email "
                    + "address and password.\n\n"
                    + "Thank you,\n"
                    + "Asset Management Team";

            emailService.sendEmail(
                    approvedUser.getEmail(),
                    subject,
                    body
            );

            logger.info(
                    "Approval email sent for user ID {}",
                    userId
            );

        } catch (MailException e) {
            logger.warn(
                    "User ID {} was approved, but the approval email "
                    + "could not be sent.",
                    userId,
                    e
            );
        }

        return approvedUser;
    }

    // Block a user
    @Transactional
    public User blockUser(Long userId) {

        Optional<User> userResult =
                userRepository.findById(userId);

        if (userResult.isEmpty()) {
            throw new IllegalArgumentException(
                    "User not found"
            );
        }

        User user = userResult.get();

        if (user.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new IllegalArgumentException(
                    "User is already blocked"
            );
        }

        user.setAccountStatus(AccountStatus.BLOCKED);

        return userRepository.save(user);
    }
}
