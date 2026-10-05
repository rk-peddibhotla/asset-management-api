
package com.ravikanth.asset_management.asset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // Get all assets and load their category at the same time
    @Query("""
            SELECT asset
            FROM Asset asset
            JOIN FETCH asset.category
            """)
    List<Asset> findAllWithCategory();
}

