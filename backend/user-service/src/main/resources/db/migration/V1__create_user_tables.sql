-- ──────────────────────────────────────────────────────────────
-- V1__create_user_tables.sql
-- User Service — initial schema
-- ──────────────────────────────────────────────────────────────

CREATE TABLE member_profile (
                                id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id        BIGINT       NOT NULL UNIQUE,   -- references app_user.id in auth-service
                                firstname      VARCHAR(100) NOT NULL,
                                lastname       VARCHAR(100) NOT NULL,
                                phone          VARCHAR(20),
                                date_of_birth  DATE,
                                avatar_url     VARCHAR(512),
                                status         ENUM('ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                                created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE coach_profile (
                               id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id         BIGINT       NOT NULL UNIQUE,  -- references app_user.id in auth-service
                               firstname       VARCHAR(100) NOT NULL,
                               lastname        VARCHAR(100) NOT NULL,
                               phone           VARCHAR(20),
                               date_of_birth   DATE,
                               avatar_url      VARCHAR(512),
                               bio             TEXT,
                               specialization  VARCHAR(255),
                               certifications  VARCHAR(512),
                               status          ENUM('ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                               created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE admin_profile (
                               id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id        BIGINT       NOT NULL UNIQUE,   -- references app_user.id in auth-service
                               firstname      VARCHAR(100) NOT NULL,
                               lastname       VARCHAR(100) NOT NULL,
                               phone          VARCHAR(20),
                               date_of_birth  DATE,
                               avatar_url     VARCHAR(512),
                               status         ENUM('ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
                               created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE health_profile (
                                id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
                                member_profile_id    BIGINT NOT NULL UNIQUE,
                                fitness_objective    VARCHAR(100),              -- e.g. WEIGHT_LOSS, MUSCLE_GAIN, ENDURANCE
                                medical_restrictions TEXT,
                                weight_kg            DECIMAL(5, 2),
                                height_cm            DECIMAL(5, 2),
                                created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (member_profile_id) REFERENCES member_profile(id) ON DELETE CASCADE
);

CREATE TABLE nfc_card (
                          id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                          member_profile_id BIGINT      NOT NULL,
                          card_uid          VARCHAR(100) NOT NULL UNIQUE, -- e.g. card001..card005 for Node-RED
                          status            ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
                          created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (member_profile_id) REFERENCES member_profile(id) ON DELETE CASCADE
);

-- Indexes for frequent lookups
CREATE INDEX idx_member_profile_user_id  ON member_profile(user_id);
CREATE INDEX idx_coach_profile_user_id   ON coach_profile(user_id);
CREATE INDEX idx_admin_profile_user_id   ON admin_profile(user_id);
CREATE INDEX idx_nfc_card_uid            ON nfc_card(card_uid);
CREATE INDEX idx_nfc_card_member         ON nfc_card(member_profile_id);