-- ============================================================
-- Veltro Activity Seed — veltro_activity database
-- 30 days of historical data for 5 members
-- member_id = auth.app_user.id
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE machine_session;
TRUNCATE TABLE gym_entry;
SET FOREIGN_KEY_CHECKS = 1;

-- Gym entries — 30 days for members 1-5 (card001-card005)
-- Each member visits 3-4 times per week with IN/OUT pairs
INSERT INTO gym_entry (member_id, card_uid, member_name, direction, timestamp, session_id) VALUES
    -- Member 1 (Alice) — regular 4 days/week
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL 29 DAY + INTERVAL  9 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'OUT', NOW() - INTERVAL 29 DAY + INTERVAL 10 HOUR, (SELECT session_id FROM (SELECT session_id FROM gym_entry WHERE card_uid='card001' AND direction='IN' ORDER BY timestamp DESC LIMIT 1) t)),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL 27 DAY + INTERVAL  7 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL 25 DAY + INTERVAL 10 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL 23 DAY + INTERVAL  8 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL 20 DAY + INTERVAL  9 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL 18 DAY + INTERVAL 11 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL 15 DAY + INTERVAL  8 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL 12 DAY + INTERVAL 10 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL 10 DAY + INTERVAL  9 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL  7 DAY + INTERVAL  8 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL  5 DAY + INTERVAL 11 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL  3 DAY + INTERVAL  9 HOUR, UUID()),
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL  1 DAY + INTERVAL  8 HOUR, UUID()),
    -- Member 2 (Bob) — 3 days/week
    (2, 'card002', 'Bob Johnson',   'IN', NOW() - INTERVAL 28 DAY + INTERVAL 12 HOUR, UUID()),
    (2, 'card002', 'Bob Johnson',   'IN', NOW() - INTERVAL 25 DAY + INTERVAL 13 HOUR, UUID()),
    (2, 'card002', 'Bob Johnson',   'IN', NOW() - INTERVAL 22 DAY + INTERVAL 11 HOUR, UUID()),
    (2, 'card002', 'Bob Johnson',   'IN', NOW() - INTERVAL 19 DAY + INTERVAL 12 HOUR, UUID()),
    (2, 'card002', 'Bob Johnson',   'IN', NOW() - INTERVAL 16 DAY + INTERVAL 13 HOUR, UUID()),
    (2, 'card002', 'Bob Johnson',   'IN', NOW() - INTERVAL 13 DAY + INTERVAL 11 HOUR, UUID()),
    (2, 'card002', 'Bob Johnson',   'IN', NOW() - INTERVAL 10 DAY + INTERVAL 12 HOUR, UUID()),
    (2, 'card002', 'Bob Johnson',   'IN', NOW() - INTERVAL  7 DAY + INTERVAL 11 HOUR, UUID()),
    (2, 'card002', 'Bob Johnson',   'IN', NOW() - INTERVAL  4 DAY + INTERVAL 12 HOUR, UUID()),
    (2, 'card002', 'Bob Johnson',   'IN', NOW() - INTERVAL  2 DAY + INTERVAL 13 HOUR, UUID()),
    -- Member 3 (Chloe) — 5 days/week, very active
    (3, 'card003', 'Chloe Bernard', 'IN', NOW() - INTERVAL 29 DAY + INTERVAL  6 HOUR, UUID()),
    (3, 'card003', 'Chloe Bernard', 'IN', NOW() - INTERVAL 28 DAY + INTERVAL  6 HOUR, UUID()),
    (3, 'card003', 'Chloe Bernard', 'IN', NOW() - INTERVAL 27 DAY + INTERVAL  6 HOUR, UUID()),
    (3, 'card003', 'Chloe Bernard', 'IN', NOW() - INTERVAL 26 DAY + INTERVAL  6 HOUR, UUID()),
    (3, 'card003', 'Chloe Bernard', 'IN', NOW() - INTERVAL 25 DAY + INTERVAL  6 HOUR, UUID()),
    (3, 'card003', 'Chloe Bernard', 'IN', NOW() - INTERVAL 22 DAY + INTERVAL  6 HOUR, UUID()),
    (3, 'card003', 'Chloe Bernard', 'IN', NOW() - INTERVAL 21 DAY + INTERVAL  6 HOUR, UUID()),
    (3, 'card003', 'Chloe Bernard', 'IN', NOW() - INTERVAL 20 DAY + INTERVAL  6 HOUR, UUID()),
    (3, 'card003', 'Chloe Bernard', 'IN', NOW() - INTERVAL 14 DAY + INTERVAL  6 HOUR, UUID()),
    (3, 'card003', 'Chloe Bernard', 'IN', NOW() - INTERVAL  7 DAY + INTERVAL  6 HOUR, UUID()),
    (3, 'card003', 'Chloe Bernard', 'IN', NOW() - INTERVAL  1 DAY + INTERVAL  6 HOUR, UUID()),
    -- Member 4 — less active (triggers LowActivityAlert demo)
    (4, 'card004', 'David Moreau',  'IN', NOW() - INTERVAL 15 DAY + INTERVAL 14 HOUR, UUID()),
    (4, 'card004', 'David Moreau',  'IN', NOW() - INTERVAL  4 DAY  + INTERVAL 14 HOUR, UUID()),
    -- Today entry for Alice (for demo)
    (1, 'card001', 'Alice Martin', 'IN',  NOW() - INTERVAL 1 HOUR, UUID()),
    (5, 'card005', 'Emma Petit',   'IN',  NOW() - INTERVAL 2 HOUR, UUID());

ALTER TABLE gym_entry AUTO_INCREMENT = 1000;

-- Machine sessions — varied types for 5 members
INSERT INTO machine_session (member_id, member_email, machine_type, duration_minutes, calories_burned, distance_km, avg_heart_rate, recorded_at) VALUES
    -- Alice (member_id=1)
    (1, 'member1@veltro.gym', 'TREADMILL', 45, 420, 6.8, 158, NOW() - INTERVAL 29 DAY),
    (1, 'member1@veltro.gym', 'BIKE',      30, 280, 12.0,142, NOW() - INTERVAL 27 DAY),
    (1, 'member1@veltro.gym', 'WEIGHTS',   50, 310, NULL,135, NOW() - INTERVAL 25 DAY),
    (1, 'member1@veltro.gym', 'TREADMILL', 40, 380, 6.1, 152, NOW() - INTERVAL 23 DAY),
    (1, 'member1@veltro.gym', 'ROWING',    25, 250, 5.0, 165, NOW() - INTERVAL 20 DAY),
    (1, 'member1@veltro.gym', 'TREADMILL', 60, 560, 9.2, 160, NOW() - INTERVAL 18 DAY),
    (1, 'member1@veltro.gym', 'BIKE',      35, 290, 14.0,140, NOW() - INTERVAL 15 DAY),
    (1, 'member1@veltro.gym', 'WEIGHTS',   55, 340, NULL,138, NOW() - INTERVAL 10 DAY),
    (1, 'member1@veltro.gym', 'TREADMILL', 45, 430, 7.0, 162, NOW() - INTERVAL  7 DAY),
    (1, 'member1@veltro.gym', 'TREADMILL', 50, 480, 7.8, 155, NOW() - INTERVAL  3 DAY),
    -- Bob (member_id=2)
    (2, 'member2@veltro.gym', 'WEIGHTS',   60, 280, NULL,130, NOW() - INTERVAL 28 DAY),
    (2, 'member2@veltro.gym', 'WEIGHTS',   65, 310, NULL,135, NOW() - INTERVAL 22 DAY),
    (2, 'member2@veltro.gym', 'ROWING',    30, 290, 6.0, 168, NOW() - INTERVAL 16 DAY),
    (2, 'member2@veltro.gym', 'WEIGHTS',   70, 350, NULL,132, NOW() - INTERVAL 10 DAY),
    (2, 'member2@veltro.gym', 'BIKE',      40, 320, 16.0,145, NOW() - INTERVAL  4 DAY),
    -- Chloe (member_id=3) — most active
    (3, 'member3@veltro.gym', 'TREADMILL', 55, 520, 8.5, 170, NOW() - INTERVAL 29 DAY),
    (3, 'member3@veltro.gym', 'BIKE',      45, 400, 18.0,162, NOW() - INTERVAL 28 DAY),
    (3, 'member3@veltro.gym', 'ROWING',    30, 300, 6.2, 172, NOW() - INTERVAL 27 DAY),
    (3, 'member3@veltro.gym', 'TREADMILL', 50, 490, 8.0, 168, NOW() - INTERVAL 22 DAY),
    (3, 'member3@veltro.gym', 'WEIGHTS',   45, 210, NULL,128, NOW() - INTERVAL 21 DAY),
    (3, 'member3@veltro.gym', 'TREADMILL', 60, 580, 9.5, 175, NOW() - INTERVAL 14 DAY),
    (3, 'member3@veltro.gym', 'BIKE',      40, 370, 16.5,160, NOW() - INTERVAL  7 DAY),
    (3, 'member3@veltro.gym', 'TREADMILL', 55, 530, 8.8, 172, NOW() - INTERVAL  1 DAY),
    -- David (member_id=4) — sporadic
    (4, 'member4@veltro.gym', 'TREADMILL', 30, 260, 4.5, 148, NOW() - INTERVAL 15 DAY),
    -- Emma (member_id=5)
    (5, 'member5@veltro.gym', 'BIKE',      35, 290, 14.0,155, NOW() - INTERVAL 20 DAY),
    (5, 'member5@veltro.gym', 'ROWING',    25, 230, 5.0, 165, NOW() - INTERVAL 12 DAY),
    (5, 'member5@veltro.gym', 'TREADMILL', 40, 360, 6.0, 158, NOW() - INTERVAL  5 DAY);

ALTER TABLE machine_session AUTO_INCREMENT = 1000;
