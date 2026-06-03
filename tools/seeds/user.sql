-- ============================================================
-- Veltro User Seed — veltro_user database
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE nfc_card;
TRUNCATE TABLE health_profile;
TRUNCATE TABLE member_profile;
TRUNCATE TABLE coach_profile;
TRUNCATE TABLE admin_profile;
SET FOREIGN_KEY_CHECKS = 1;

-- Member profiles (user_id links to auth.app_user.id)
INSERT INTO member_profile (id, user_id, firstname, lastname, phone, date_of_birth, status) VALUES
    (1,  1,  'Alice',    'Martin',   '+213-555-0101', '1995-03-14', 'ACTIVE'),
    (2,  2,  'Bob',      'Johnson',  '+213-555-0102', '1988-07-22', 'ACTIVE'),
    (3,  3,  'Chloe',    'Bernard',  '+213-555-0103', '1997-11-05', 'ACTIVE'),
    (4,  4,  'David',    'Moreau',   '+213-555-0104', '1992-01-30', 'ACTIVE'),
    (5,  5,  'Emma',     'Petit',    '+213-555-0105', '2000-06-18', 'ACTIVE'),
    (6,  6,  'Frank',    'Durand',   '+213-555-0106', '1985-09-09', 'SUSPENDED'),
    (7,  7,  'Grace',    'Laurent',  '+213-555-0107', '1993-12-25', 'ACTIVE'),
    (8,  8,  'Hugo',     'Simon',    '+213-555-0108', '1990-04-17', 'ACTIVE'),
    (9,  9,  'Iris',     'Michel',   '+213-555-0109', '1998-08-03', 'ACTIVE'),
    (10, 10, 'Jules',    'Lefebvre', '+213-555-0110', '1987-02-28', 'ACTIVE');

ALTER TABLE member_profile AUTO_INCREMENT = 100;

-- Coach profiles
INSERT INTO coach_profile (id, user_id, firstname, lastname, phone, bio, specialization, certifications, status) VALUES
    (11, 11, 'Sophie',   'Rousseau',  '+213-555-0201', 'Certified personal trainer with 8 years experience', 'Strength & Conditioning', 'NASM-CPT, CSCS', 'ACTIVE'),
    (12, 12, 'Thomas',   'Garnier',   '+213-555-0202', 'Yoga and flexibility specialist', 'Yoga & Mindfulness', 'RYT-500', 'ACTIVE'),
    (13, 13, 'Laura',    'Fontaine',  '+213-555-0203', 'Cardio and endurance coach', 'Cardio & HIIT', 'ACSM-CPT', 'ACTIVE');

ALTER TABLE coach_profile AUTO_INCREMENT = 100;

-- Admin profile
INSERT INTO admin_profile (id, user_id, firstname, lastname, phone, status) VALUES
    (14, 14, 'Admin', 'Veltro', '+213-555-0301', 'ACTIVE');

ALTER TABLE admin_profile AUTO_INCREMENT = 100;

-- Health profiles for 5 members
INSERT INTO health_profile (member_profile_id, fitness_objective, medical_restrictions, weight_kg, height_cm) VALUES
    (1,  'WEIGHT_LOSS',  'None',                   75.50, 168.00),
    (2,  'MUSCLE_GAIN',  'Lower back pain',        82.00, 180.00),
    (3,  'ENDURANCE',    'None',                   58.00, 162.00),
    (4,  'WEIGHT_LOSS',  'Knee issue — no jumps',  90.00, 175.00),
    (5,  'MUSCLE_GAIN',  'None',                   55.00, 158.00);

-- NFC Cards (card001–card010 matching Node-RED flows card001–card005)
INSERT INTO nfc_card (member_profile_id, card_uid, status) VALUES
    (1,  'card001', 'ACTIVE'),
    (2,  'card002', 'ACTIVE'),
    (3,  'card003', 'ACTIVE'),
    (4,  'card004', 'ACTIVE'),
    (5,  'card005', 'ACTIVE'),
    (6,  'card006', 'INACTIVE'),
    (7,  'card007', 'ACTIVE'),
    (8,  'card008', 'ACTIVE'),
    (9,  'card009', 'ACTIVE'),
    (10, 'card010', 'ACTIVE');
