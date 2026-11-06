package com.ravikanth.asset_management.resource;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository
        extends JpaRepository<Resource, Long> {

    // Get all resources and load their category at the same time
    @Query("""
            SELECT resource
            FROM Resource resource
            JOIN FETCH resource.category
            """)
    List<Resource> findAllWithCategory();

    // Lock a resource during borrowing approval
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT resource
            FROM Resource resource
            WHERE resource.id = :resourceId
            """)
    Optional<Resource> findByIdForUpdate(
            @Param("resourceId") Long resourceId
    );
}