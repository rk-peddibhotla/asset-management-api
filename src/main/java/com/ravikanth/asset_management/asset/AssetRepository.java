
package com.ravikanth.asset_management.asset;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // Get all assets and load their category at the same time
    @Query("""
            SELECT asset
            FROM Asset asset
            JOIN FETCH asset.category
            """)
    List<Asset> findAllWithCategory();

    // Lock an asset during order approval
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT asset
            FROM Asset asset
            WHERE asset.id = :assetId
            """)
    Optional<Asset> findByIdForUpdate(
            @Param("assetId") Long assetId
    );
}
