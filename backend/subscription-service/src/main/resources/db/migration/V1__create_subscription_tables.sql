-- V1__create_subscription_tables.sql
-- Subscription Service — initial schema

CREATE TABLE subscription (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id           BIGINT       NOT NULL,
    member_email        VARCHAR(255),
    plan                ENUM('TRIAL','SESSION','MONTHLY','ANNUAL') NOT NULL,
    status              ENUM('ACTIVE','PAUSED','CANCELLED','EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    start_date          DATE         NOT NULL,
    end_date            DATE         NOT NULL,
    auto_renew          BOOLEAN      NOT NULL DEFAULT FALSE,
    paused_months_used  INT          NOT NULL DEFAULT 0,
    pause_started_at    DATE,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_subscription_member_id (member_id),
    INDEX idx_subscription_status    (status),
    INDEX idx_subscription_end_date  (end_date)
);

CREATE TABLE payment_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT          NOT NULL,
    amount          DECIMAL(10, 2)  NOT NULL,
    paid_at         DATETIME        NOT NULL,
    method          ENUM('CARD','CASH','TRANSFER','AUTO_RENEWAL') NOT NULL,
    invoice_ref     VARCHAR(100)    NOT NULL UNIQUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_payment_subscription_id (subscription_id),
    CONSTRAINT fk_payment_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscription(id) ON DELETE CASCADE
);
