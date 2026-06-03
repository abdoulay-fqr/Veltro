-- V1__create_shop_tables.sql

CREATE TABLE product (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    version        BIGINT         NOT NULL DEFAULT 0,
    name           VARCHAR(255)   NOT NULL,
    description    TEXT,
    category       ENUM('SUPPLEMENT','CLOTHING','EQUIPMENT') NOT NULL,
    price          DECIMAL(10,2)  NOT NULL,
    stock_quantity INT            NOT NULL DEFAULT 0,
    image_url      VARCHAR(512),
    active         BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_product_category (category),
    INDEX idx_product_active   (active)
);

CREATE TABLE shop_order (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT         NOT NULL,
    status           ENUM('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    total_amount     DECIMAL(10,2)  NOT NULL,
    shipping_address TEXT,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_order_member_id (member_id),
    INDEX idx_order_status    (status)
);

CREATE TABLE order_item (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(10,2)  NOT NULL,

    INDEX idx_item_order_id (order_id),
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES shop_order(id) ON DELETE CASCADE
);
