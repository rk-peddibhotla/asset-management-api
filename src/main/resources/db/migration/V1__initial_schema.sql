CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    account_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_users_role
        CHECK (role IN ('STUDENT', 'ADMIN')),

    CONSTRAINT chk_users_account_status
        CHECK (account_status IN ('UNAPPROVED', 'ACTIVE', 'BLOCKED'))
);


CREATE TABLE asset_categories (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    sub_category VARCHAR(100) NOT NULL,

    CONSTRAINT uq_asset_categories_category_subcategory
        UNIQUE (category, sub_category)
);


CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    manufacturer VARCHAR(100),
    price DECIMAL(12, 2) NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_assets_category
        FOREIGN KEY (category_id)
        REFERENCES asset_categories(id),

    CONSTRAINT chk_assets_price
        CHECK (price >= 0)
);


CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,

    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    due_at TIMESTAMP,
    returned_at TIMESTAMP,

    fine_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_orders_asset
        FOREIGN KEY (asset_id)
        REFERENCES assets(id),

    CONSTRAINT chk_orders_status
        CHECK (
            status IN (
                'PENDING',
                'APPROVED',
                'ACTIVE',
                'RETURNED',
                'REJECTED'
            )
        ),

    CONSTRAINT chk_orders_fine_amount
        CHECK (fine_amount >= 0)
);


CREATE INDEX idx_assets_category_id
    ON assets(category_id);

CREATE INDEX idx_orders_user_id
    ON orders(user_id);

CREATE INDEX idx_orders_asset_id
    ON orders(asset_id);

CREATE INDEX idx_orders_status
    ON orders(status);