-- V1__create_booking_tables.sql — Booking Service initial schema

CREATE TABLE course (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    coach_id         BIGINT          NOT NULL,
    name             VARCHAR(255)    NOT NULL,
    description      TEXT,
    date_time        DATETIME        NOT NULL,
    duration_minutes INT             NOT NULL DEFAULT 60,
    capacity         INT             NOT NULL,
    enrolled_count   INT             NOT NULL DEFAULT 0,
    level            ENUM('BEGINNER','INTERMEDIATE','ADVANCED') NOT NULL,
    room             VARCHAR(100),
    status           ENUM('SCHEDULED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'SCHEDULED',
    reminder_sent    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_course_coach_id (coach_id),
    INDEX idx_course_date_time (date_time),
    INDEX idx_course_status (status),
    INDEX idx_course_level (level)
);

CREATE TABLE course_registration (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id           BIGINT          NOT NULL,
    member_id           BIGINT          NOT NULL,
    member_email        VARCHAR(255),
    status              ENUM('BOOKED','WAITLISTED','CANCELLED') NOT NULL DEFAULT 'BOOKED',
    waitlist_position   INT,
    registered_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at        DATETIME,
    promoted_at         DATETIME,
    cancellation_reason VARCHAR(500),

    UNIQUE KEY uq_course_member (course_id, member_id),
    INDEX idx_reg_course_id  (course_id),
    INDEX idx_reg_member_id  (member_id),
    INDEX idx_reg_status     (status),
    CONSTRAINT fk_reg_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

CREATE TABLE attendance (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id                 BIGINT    NOT NULL,
    member_id                 BIGINT    NOT NULL,
    member_email              VARCHAR(255),
    present                   BOOLEAN   NOT NULL DEFAULT TRUE,
    marked_at                 DATETIME,
    marked_by_coach_id        BIGINT,
    processed_for_suspension  BOOLEAN   NOT NULL DEFAULT FALSE,

    UNIQUE KEY uq_attendance_course_member (course_id, member_id),
    INDEX idx_attendance_course_id  (course_id),
    INDEX idx_attendance_member_id  (member_id),
    CONSTRAINT fk_att_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);
