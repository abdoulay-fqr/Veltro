-- V1__create_activity_tables.sql — Activity Service initial schema

CREATE TABLE gym_entry (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id    BIGINT       NOT NULL,
    card_uid     VARCHAR(100) NOT NULL,
    member_name  VARCHAR(255),
    member_email VARCHAR(255),
    direction    ENUM('IN','OUT') NOT NULL,
    timestamp    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    session_id   VARCHAR(36),

    INDEX idx_entry_member_id  (member_id),
    INDEX idx_entry_card_uid   (card_uid),
    INDEX idx_entry_timestamp  (timestamp),
    INDEX idx_entry_session_id (session_id),
    INDEX idx_entry_direction  (direction)
);

CREATE TABLE machine_session (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT        NOT NULL,
    member_email     VARCHAR(255),
    machine_type     ENUM('TREADMILL','BIKE','ROWING','WEIGHTS','OTHER') NOT NULL,
    duration_minutes INT           NOT NULL,
    calories_burned  INT,
    distance_km      DOUBLE,
    avg_heart_rate   INT,
    recorded_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_session_member_id   (member_id),
    INDEX idx_session_recorded_at (recorded_at),
    INDEX idx_session_machine_type(machine_type)
);
