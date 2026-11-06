package com.ravikanth.asset_management.resource;

import jakarta.persistence.*;

@Entity
@Table(
        name = "resource_categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_resource_categories_category_subcategory",
                        columnNames = {"category", "sub_category"}
                )
        }
)
public class ResourceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "sub_category", nullable = false, length = 100)
    private String subCategory;

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