
package com.ravikanth.asset_management.borrowing;

import com.ravikanth.asset_management.organization.OrganizationSettings;
import com.ravikanth.asset_management.organization.OrganizationSettingsRepository;
import com.ravikanth.asset_management.resource.Resource;
import com.ravikanth.asset_management.resource.ResourceRepository;
import com.ravikanth.asset_management.resource.ResourceStatus;
import com.ravikanth.asset_management.user.AccountStatus;
import com.ravikanth.asset_management.user.User;
import com.ravikanth.asset_management.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BorrowingRequestService {

    private final BorrowingRequestRepository borrowingRequestRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final OrganizationSettingsRepository settingsRepository;
    private final EntityManager entityManager;

    public BorrowingRequestService(
            BorrowingRequestRepository borrowingRequestRepository,
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            OrganizationSettingsRepository settingsRepository,
            EntityManager entityManager
    ) {
        this.borrowingRequestRepository = borrowingRequestRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.settingsRepository = settingsRepository;
        this.entityManager = entityManager;
    }

    // Create a borrowing request
    @Transactional
    public BorrowingResponse createBorrowing(
            Long userId,
            Long resourceId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"
                ));

        validateActiveUser(user);

        OrganizationSettings settings = getSettings();

        Resource resource;

        if (settings.isBorrowingApprovalRequired()) {
            // Pending requests do not consume an active borrowing slot.
            resource = resourceRepository.findByIdForUpdate(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Resource not found"
                    ));

            validateResourceAvailable(resource, resourceId);

            BorrowingRequest borrowing = createBorrowingRecord(
                    user,
                    resource,
                    settings
            );

            borrowing.setStatus(BorrowingStatus.PENDING);

            BorrowingRequest savedBorrowing =
                    borrowingRequestRepository.save(borrowing);

            return BorrowingResponse.from(savedBorrowing);
        }

        // Immediate activation: lock the user first, then the resource.
        entityManager.lock(user, LockModeType.PESSIMISTIC_WRITE);

        // Recheck account status after acquiring the lock.
        validateActiveUser(user);

        resource = resourceRepository.findByIdForUpdate(resourceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resource not found"
                ));

        validateResourceAvailable(resource, resourceId);
        validateBorrowingLimit(userId, settings);

        BorrowingRequest borrowing = createBorrowingRecord(
                user,
                resource,
                settings
        );

        activateBorrowing(borrowing);

        BorrowingRequest savedBorrowing =
                borrowingRequestRepository.save(borrowing);

        return BorrowingResponse.from(savedBorrowing);
    }

    // Get all borrowing requests for a user
    @Transactional(readOnly = true)
    public List<BorrowingRequest> getBorrowingsForUser(Long userId) {
        return borrowingRequestRepository
                .findAllByUserIdWithDetails(userId);
    }

    // Get all borrowing requests
    @Transactional(readOnly = true)
    public List<BorrowingRequest> getAllBorrowings() {
        return borrowingRequestRepository
                .findAllBorrowingsWithDetails();
    }

    // Get all pending borrowing requests
    @Transactional(readOnly = true)
    public List<BorrowingRequest> getPendingBorrowings() {
        return borrowingRequestRepository
                .findAllPendingBorrowingsWithDetails();
    }

    // Approve a pending borrowing request
    @Transactional
    public BorrowingResponse approveBorrowing(Long borrowingId) {

        // Lock the request first to prevent concurrent lifecycle changes.
        BorrowingRequest borrowing = borrowingRequestRepository
                .findByIdForUpdate(borrowingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Borrowing request not found"
                ));

        Long userId = borrowing.getUser().getId();
        Long resourceId = borrowing.getResource().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"
                ));

        // Protect the user's active borrowing limit.
        entityManager.lock(user, LockModeType.PESSIMISTIC_WRITE);

        Resource resource = resourceRepository.findByIdForUpdate(resourceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resource not found"
                ));

        if (borrowing.getStatus() != BorrowingStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending borrowing requests can be approved"
            );
        }

        validateActiveUser(user);
        validateResourceAvailable(resource, resourceId);

        OrganizationSettings settings = getSettings();

        validateBorrowingLimit(userId, settings);

        // Use the snapshots saved when the request was created.
        activateBorrowing(borrowing);

        BorrowingRequest savedBorrowing =
                borrowingRequestRepository.save(borrowing);

        return BorrowingResponse.from(savedBorrowing);
    }

    // Reject a pending borrowing request
    @Transactional
    public BorrowingResponse rejectBorrowing(Long borrowingId) {

        // Lock the request before checking or changing its status.
        BorrowingRequest borrowing = borrowingRequestRepository
                .findByIdForUpdate(borrowingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Borrowing request not found"
                ));

        if (borrowing.getStatus() != BorrowingStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending borrowing requests can be rejected"
            );
        }

        borrowing.setStatus(BorrowingStatus.REJECTED);

        BorrowingRequest savedBorrowing =
                borrowingRequestRepository.save(borrowing);

        return BorrowingResponse.from(savedBorrowing);
    }

    // Return an active borrowing
    @Transactional
    public BorrowingResponse returnBorrowing(
            Long borrowingId,
            Long userId
    ) {
        // Lock the request to prevent duplicate or concurrent returns.
        BorrowingRequest borrowing = borrowingRequestRepository
                .findByIdForUpdate(borrowingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Borrowing request not found"
                ));

        if (!borrowing.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "User cannot return this borrowing"
            );
        }

        if (borrowing.getStatus() != BorrowingStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Only active borrowings can be returned"
            );
        }

        if (borrowing.getDueAt() == null) {
            throw new IllegalStateException(
                    "Active borrowing has no due date"
            );
        }

        if (borrowing.getDailyLateFeeSnapshot() == null) {
            throw new IllegalStateException(
                    "Borrowing has no daily late-fee snapshot"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        borrowing.setStatus(BorrowingStatus.RETURNED);
        borrowing.setReturnedAt(now);

        if (now.isAfter(borrowing.getDueAt())) {

            long overdueDays = ChronoUnit.DAYS.between(
                    borrowing.getDueAt().toLocalDate(),
                    now.toLocalDate()
            );

            BigDecimal lateFee =
                    borrowing.getDailyLateFeeSnapshot().multiply(
                            BigDecimal.valueOf(overdueDays)
                    );

            borrowing.setLateFeeAmount(lateFee);

        } else {
            borrowing.setLateFeeAmount(BigDecimal.ZERO);
        }

        BorrowingRequest savedBorrowing =
                borrowingRequestRepository.save(borrowing);

        return BorrowingResponse.from(savedBorrowing);
    }

    // Create a borrowing record and save the current settings as snapshots
    private BorrowingRequest createBorrowingRecord(
            User user,
            Resource resource,
            OrganizationSettings settings
    ) {
        BorrowingRequest borrowing = new BorrowingRequest();

        borrowing.setUser(user);
        borrowing.setResource(resource);
        borrowing.setStatus(BorrowingStatus.PENDING);

        borrowing.setDailyLateFeeSnapshot(
                settings.getDailyLateFee()
        );

        borrowing.setBorrowingDurationDaysSnapshot(
                settings.getBorrowingDurationDays()
        );

        borrowing.setLateFeeAmount(BigDecimal.ZERO);

        return borrowing;
    }

    // Activate a borrowing using its saved duration
    private void activateBorrowing(BorrowingRequest borrowing) {

        Integer durationDays =
                borrowing.getBorrowingDurationDaysSnapshot();

        if (durationDays == null || durationDays < 1) {
            throw new IllegalStateException(
                    "Borrowing has an invalid duration snapshot"
            );
        }

        if (borrowing.getDailyLateFeeSnapshot() == null
                || borrowing.getDailyLateFeeSnapshot()
                        .compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Borrowing has an invalid daily late-fee snapshot"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        borrowing.setStatus(BorrowingStatus.ACTIVE);
        borrowing.setActivatedAt(now);
        borrowing.setDueAt(now.plusDays(durationDays));
    }

    // Validate that the user account is active
    private void validateActiveUser(User user) {

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "User account is not active"
            );
        }
    }

    // Validate that a resource can be borrowed
    private void validateResourceAvailable(
            Resource resource,
            Long resourceId
    ) {
        if (resource.getStatus() != ResourceStatus.AVAILABLE) {
            throw new IllegalArgumentException(
                    "Resource is unavailable"
            );
        }

        boolean alreadyBorrowed =
                borrowingRequestRepository
                        .existsActiveBorrowingForResource(resourceId);

        if (alreadyBorrowed) {
            throw new IllegalArgumentException(
                    "Resource is currently borrowed"
            );
        }
    }

    // Enforce the configured active borrowing limit
    private void validateBorrowingLimit(
            Long userId,
            OrganizationSettings settings
    ) {
        long activeBorrowingCount =
                borrowingRequestRepository
                        .countActiveBorrowingsByUserId(userId);

        if (activeBorrowingCount >= settings.getMaxActiveBorrowings()) {
            throw new IllegalArgumentException(
                    "User cannot have more than "
                            + settings.getMaxActiveBorrowings()
                            + " active borrowings"
            );
        }
    }

    // Retrieve the organization's settings
    private OrganizationSettings getSettings() {
        return settingsRepository.findById((short) 1)
                .orElseThrow(() -> new IllegalStateException(
                        "Organization settings have not been initialized"
                ));
    }
}
