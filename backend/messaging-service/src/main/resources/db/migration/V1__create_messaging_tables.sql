-- V1__create_messaging_tables.sql

CREATE TABLE conversation (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id             BIGINT       NOT NULL,
    coach_id              BIGINT       NOT NULL,
    last_message_at       DATETIME,
    last_message_preview  VARCHAR(100),
    unread_count_member   INT          NOT NULL DEFAULT 0,
    unread_count_coach    INT          NOT NULL DEFAULT 0,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uq_member_coach (member_id, coach_id),
    INDEX idx_conv_member_id (member_id),
    INDEX idx_conv_coach_id  (coach_id)
);

CREATE TABLE message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT   NOT NULL,
    sender_id       BIGINT   NOT NULL,
    sender_role     ENUM('MEMBER','COACH') NOT NULL,
    content         TEXT     NOT NULL,
    sent_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at         DATETIME,

    INDEX idx_msg_conversation_id (conversation_id),
    INDEX idx_msg_sender_id       (sender_id),
    INDEX idx_msg_sent_at         (sent_at),
    CONSTRAINT fk_msg_conversation FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE
);
