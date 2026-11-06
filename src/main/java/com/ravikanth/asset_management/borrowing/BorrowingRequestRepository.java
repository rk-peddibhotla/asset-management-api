
package com.ravikanth.asset_management.borrowing;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BorrowingRequestRepository
        extends JpaRepository<BorrowingRequest, Long> {

    // Count the user's currently active borrowings
    @Query("""
            SELECT COUNT(b)
            FROM BorrowingRequest b
            WHERE b.user.id = :userId
              AND b.status = com.ravikanth.asset_management.borrowing.BorrowingStatus.ACTIVE
            """)
    long countActiveBorrowingsByUserId(
            @Param("userId") Long userId
    );

    // Check whether a resource is currently borrowed
    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM BorrowingRequest b
            WHERE b.resource.id = :resourceId
              AND b.status = com.ravikanth.asset_management.borrowing.BorrowingStatus.ACTIVE
            """)
    boolean existsActiveBorrowingForResource(
            @Param("resourceId") Long resourceId
    );

    // Find one borrowing request and load its user and resource
    @Query("""
            SELECT b
            FROM BorrowingRequest b
            JOIN FETCH b.user
            JOIN FETCH b.resource
            WHERE b.id = :borrowingId
            """)
    Optional<BorrowingRequest> findByIdWithDetails(
            @Param("borrowingId") Long borrowingId
    );

    // Lock one borrowing request during approval, rejection, or return
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM BorrowingRequest b
            WHERE b.id = :borrowingId
            """)
    Optional<BorrowingRequest> findByIdForUpdate(
            @Param("borrowingId") Long borrowingId
    );

    // Get all borrowings for a specific user, newest first
    @Query("""
            SELECT b
            FROM BorrowingRequest b
            JOIN FETCH b.user
            JOIN FETCH b.resource
            WHERE b.user.id = :userId
            ORDER BY b.createdAt DESC
            """)
    List<BorrowingRequest> findAllByUserIdWithDetails(
            @Param("userId") Long userId
    );

    // Get pending borrowing requests for administrators
    @Query("""
            SELECT b
            FROM BorrowingRequest b
            JOIN FETCH b.user
            JOIN FETCH b.resource
            WHERE b.status = com.ravikanth.asset_management.borrowing.BorrowingStatus.PENDING
            ORDER BY b.createdAt DESC
            """)
    List<BorrowingRequest> findAllPendingBorrowingsWithDetails();

    // Get complete borrowing history for administrators
    @Query("""
            SELECT b
            FROM BorrowingRequest b
            JOIN FETCH b.user
            JOIN FETCH b.resource
            ORDER BY b.createdAt DESC
            """)
    List<BorrowingRequest> findAllBorrowingsWithDetails();
}
