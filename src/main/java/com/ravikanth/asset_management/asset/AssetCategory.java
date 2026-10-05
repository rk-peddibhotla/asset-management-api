
package com.ravikanth.asset_management.asset;

import jakarta.persistence.*;

@Entity
@Table(
        name = "asset_categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_asset_categories_category_subcategory",
                        columnNames = {"category", "sub_category"}
                )
        }
)
public class AssetCategory {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Main category
    @Column(nullable = false, length = 100)
    private String category;

    // Sub-category inside the main category
    @Column(name = "sub_category", nullable = false, length = 100)
    private String subCategory;


    // Getters and setters

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }
}

