CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(40) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(120),
    avatar_url VARCHAR(255),
    role VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    token_version INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT ck_user_role CHECK (role IN ('ADMIN','INVENTORY_MANAGER','CASHIER','MEMBER')),
    CONSTRAINT ck_user_status CHECK (status IN ('ACTIVE','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE auth_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    jti CHAR(36) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at DATETIME(3) NOT NULL,
    revoked_at DATETIME(3),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_session_jti UNIQUE (jti),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_session_user (user_id),
    INDEX idx_session_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    code VARCHAR(40) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_category_name UNIQUE (name),
    CONSTRAINT uk_category_code UNIQUE (code),
    CONSTRAINT ck_category_status CHECK (status IN ('ENABLED','DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL,
    barcode VARCHAR(64) NOT NULL,
    name VARCHAR(120) NOT NULL,
    category_id BIGINT NOT NULL,
    specification VARCHAR(120),
    unit VARCHAR(20) NOT NULL,
    purchase_price DECIMAL(12,2) NOT NULL,
    sale_price DECIMAL(12,2) NOT NULL,
    image_url VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ON_SALE',
    low_stock_threshold INT NOT NULL DEFAULT 10,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_product_code UNIQUE (code),
    CONSTRAINT uk_product_barcode UNIQUE (barcode),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT ck_product_prices CHECK (purchase_price >= 0 AND sale_price >= 0),
    CONSTRAINT ck_product_status CHECK (status IN ('ON_SALE','OFF_SALE')),
    INDEX idx_product_name (name),
    INDEX idx_product_category_status (category_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(120) NOT NULL,
    contact_name VARCHAR(80),
    phone VARCHAR(30),
    email VARCHAR(120),
    address VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(500),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_supplier_code UNIQUE (code),
    CONSTRAINT uk_supplier_name UNIQUE (name),
    CONSTRAINT ck_supplier_status CHECK (status IN ('ENABLED','DISABLED')),
    INDEX idx_supplier_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inventory (
    product_id BIGINT PRIMARY KEY,
    current_qty INT NOT NULL DEFAULT 0,
    reserved_qty INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT ck_inventory_nonnegative CHECK (current_qty >= 0 AND reserved_qty >= 0 AND reserved_qty <= current_qty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE purchase_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL,
    supplier_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    created_by BIGINT NOT NULL,
    confirmed_by BIGINT,
    confirmed_at DATETIME(3),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_purchase_order_no UNIQUE (order_no),
    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id),
    CONSTRAINT fk_purchase_created_by FOREIGN KEY (created_by) REFERENCES sys_user(id),
    CONSTRAINT fk_purchase_confirmed_by FOREIGN KEY (confirmed_by) REFERENCES sys_user(id),
    CONSTRAINT ck_purchase_status CHECK (status IN ('DRAFT','WAREHOUSED','CANCELLED')),
    INDEX idx_purchase_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE purchase_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_code VARCHAR(40) NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    specification VARCHAR(120),
    unit VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_purchase_item_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_item_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT uk_purchase_item_product UNIQUE (purchase_order_id, product_id),
    CONSTRAINT ck_purchase_item_values CHECK (quantity > 0 AND unit_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sale_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL,
    idempotency_key VARCHAR(80) NOT NULL,
    channel VARCHAR(16) NOT NULL,
    member_id BIGINT,
    cashier_id BIGINT,
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING_PAYMENT',
    total_amount DECIMAL(14,2) NOT NULL,
    paid_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    refund_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(24),
    paid_at DATETIME(3),
    cancelled_at DATETIME(3),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_sale_order_no UNIQUE (order_no),
    CONSTRAINT uk_sale_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_sale_member FOREIGN KEY (member_id) REFERENCES sys_user(id),
    CONSTRAINT fk_sale_cashier FOREIGN KEY (cashier_id) REFERENCES sys_user(id),
    CONSTRAINT ck_sale_channel CHECK (channel IN ('WEB_POS','MINIAPP')),
    CONSTRAINT ck_sale_status CHECK (status IN ('PENDING_PAYMENT','PAID','CANCELLED','PARTIALLY_RETURNED','RETURNED')),
    INDEX idx_sale_member_created (member_id, created_at),
    INDEX idx_sale_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sale_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_code VARCHAR(40) NOT NULL,
    barcode VARCHAR(64) NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    specification VARCHAR(120),
    unit VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    returned_qty INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(12,2) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_sale_item_order FOREIGN KEY (sale_order_id) REFERENCES sale_order(id),
    CONSTRAINT fk_sale_item_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT uk_sale_item_product UNIQUE (sale_order_id, product_id),
    CONSTRAINT ck_sale_item_values CHECK (quantity > 0 AND returned_qty >= 0 AND returned_qty <= quantity AND unit_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sale_return (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    return_no VARCHAR(40) NOT NULL,
    sale_order_id BIGINT NOT NULL,
    idempotency_key VARCHAR(80) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    operator_id BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_return_no UNIQUE (return_no),
    CONSTRAINT uk_return_idempotency UNIQUE (idempotency_key),
    CONSTRAINT fk_return_order FOREIGN KEY (sale_order_id) REFERENCES sale_order(id),
    CONSTRAINT fk_return_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sale_return_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_return_id BIGINT NOT NULL,
    sale_item_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_return_item_return FOREIGN KEY (sale_return_id) REFERENCES sale_return(id),
    CONSTRAINT fk_return_item_sale FOREIGN KEY (sale_item_id) REFERENCES sale_item(id),
    CONSTRAINT fk_return_item_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT ck_return_item_qty CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inventory_movement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    change_qty INT NOT NULL,
    change_reserved_qty INT NOT NULL DEFAULT 0,
    before_qty INT NOT NULL,
    after_qty INT NOT NULL,
    before_reserved_qty INT NOT NULL,
    after_reserved_qty INT NOT NULL,
    business_type VARCHAR(32) NOT NULL,
    business_ref VARCHAR(80) NOT NULL,
    operator_id BIGINT NOT NULL,
    remark VARCHAR(255),
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_movement_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_movement_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id),
    CONSTRAINT uk_movement_business UNIQUE (product_id, business_type, business_ref),
    INDEX idx_movement_product_created (product_id, created_at),
    INDEX idx_movement_business (business_type, business_ref)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE system_config (
    config_key VARCHAR(80) PRIMARY KEY,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(255),
    updated_by BIGINT NOT NULL,
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_config_user FOREIGN KEY (updated_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(40),
    action VARCHAR(80) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(80),
    detail VARCHAR(1000),
    ip_address VARCHAR(64),
    success BOOLEAN NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_audit_user_created (user_id, created_at),
    INDEX idx_audit_action_created (action, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

