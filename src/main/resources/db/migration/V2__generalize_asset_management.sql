
-- V2__generalize_asset_management.sql
--
-- Generalize the existing asset management schema into a
-- configurable resource borrowing platform.
--
-- Existing records and relationships are preserved.
-- No existing tables are dropped.

-- =========================================================
-- 1. Rename asset_categories to resource_categories
-- =========================================================

ALTER TABLE asset_categories
    RENAME TO resource_categories;

ALTER TABLE resource_categories
    RENAME CONSTRAINT asset_categories_pkey
    TO resource_categories_pkey;

ALTER TABLE resource_categories
    RENAME CONSTRAINT uq_asset_categories_category_subcategory
    TO uq_resource_categories_category_subcategory;


-- =========================================================
-- 2. Rename assets to resources
-- =========================================================

ALTER TABLE assets
    RENAME TO resources;

ALTER TABLE resources
    RENAME CONSTRAINT assets_pkey
    TO resources_pkey;

ALTER TABLE resources
    RENAME CONSTRAINT fk_assets_category
    TO fk_resources_category;

ALTER TABLE resources
    RENAME CONSTRAINT chk_assets_price
    TO chk_resources_purchase_price;

ALTER INDEX idx_assets_category_id
    RENAME TO idx_resources_category_id;


-- =========================================================
-- 3. Update resource columns
-- =========================================================

ALTER TABLE resources
    RENAME COLUMN price TO purchase_price;

ALTER TABLE resources
    ALTER COLUMN purchase_price DROP NOT NULL;

ALTER TABLE resources
    ADD COLUMN description TEXT;

ALTER TABLE resources
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE';

ALTER TABLE resources
    ADD CONSTRAINT chk_resources_status
    CHECK (status IN ('AVAILABLE', 'UNAVAILABLE'));


-- =========================================================
-- 4. Rename orders to borrowing_requests
-- =========================================================

ALTER TABLE orders
    RENAME TO borrowing_requests;

ALTER TABLE borrowing_requests
    RENAME CONSTRAINT orders_pkey
    TO borrowing_requests_pkey;

ALTER TABLE borrowing_requests
    RENAME CONSTRAINT fk_orders_user
    TO fk_borrowing_requests_user;

ALTER TABLE borrowing_requests
    RENAME CONSTRAINT fk_orders_asset
    TO fk_borrowing_requests_resource;

ALTER TABLE borrowing_requests
    RENAME CONSTRAINT chk_orders_status
    TO chk_borrowing_requests_status;

ALTER TABLE borrowing_requests
    RENAME CONSTRAINT chk_orders_fine_amount
    TO chk_borrowing_requests_late_fee_amount;


-- =========================================================
-- 5. Update borrowing request columns and constraints
-- =========================================================

ALTER TABLE borrowing_requests
    RENAME COLUMN asset_id TO resource_id;

ALTER TABLE borrowing_requests
    RENAME COLUMN approved_at TO activated_at;

ALTER TABLE borrowing_requests
    RENAME COLUMN fine_amount TO late_fee_amount;

ALTER TABLE borrowing_requests
    DROP CONSTRAINT chk_borrowing_requests_status;

ALTER TABLE borrowing_requests
    ADD CONSTRAINT chk_borrowing_requests_status
    CHECK (
        status IN (
            'PENDING',
            'ACTIVE',
            'RETURNED',
            'REJECTED'
        )
    );

ALTER INDEX idx_orders_user_id
    RENAME TO idx_borrowing_requests_user_id;

ALTER INDEX idx_orders_asset_id
    RENAME TO idx_borrowing_requests_resource_id;

ALTER INDEX idx_orders_status
    RENAME TO idx_borrowing_requests_status;


-- =========================================================
-- 6. Update user roles: STUDENT to USER
-- =========================================================

ALTER TABLE users
    DROP CONSTRAINT chk_users_role;

UPDATE users
SET role = 'USER'
WHERE role = 'STUDENT';

ALTER TABLE users
    ADD CONSTRAINT chk_users_role
    CHECK (role IN ('USER', 'ADMIN'));


-- =========================================================
-- 7. Create organization settings
-- =========================================================

CREATE TABLE organization_settings (
    id SMALLINT PRIMARY KEY DEFAULT 1,

    organization_name VARCHAR(150) NOT NULL
        DEFAULT 'Resource Management Portal',

    resource_label VARCHAR(50) NOT NULL
        DEFAULT 'Resource',

    max_active_borrowings INTEGER NOT NULL
        DEFAULT 3,

    borrowing_duration_days INTEGER NOT NULL
        DEFAULT 10,

    daily_late_fee DECIMAL(12, 2) NOT NULL
        DEFAULT 50.00,

    borrowing_approval_required BOOLEAN NOT NULL
        DEFAULT TRUE,

    account_approval_required BOOLEAN NOT NULL
        DEFAULT TRUE,

    email_notifications_enabled BOOLEAN NOT NULL
        DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_organization_settings_singleton
        CHECK (id = 1),

    CONSTRAINT chk_organization_settings_name
        CHECK (LENGTH(TRIM(organization_name)) > 0),

    CONSTRAINT chk_organization_settings_resource_label
        CHECK (LENGTH(TRIM(resource_label)) > 0),

    CONSTRAINT chk_organization_settings_max_borrowings
        CHECK (max_active_borrowings >= 1),

    CONSTRAINT chk_organization_settings_duration
        CHECK (borrowing_duration_days >= 1),

    CONSTRAINT chk_organization_settings_late_fee
        CHECK (daily_late_fee >= 0)
);


-- =========================================================
-- 8. Insert default organization settings
-- =========================================================

INSERT INTO organization_settings (id)
VALUES (1);
