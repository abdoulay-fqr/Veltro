-- ============================================================
-- Veltro Booking Seed — veltro_booking database
-- coach_id = auth.app_user.id (11=Sophie, 12=Thomas, 13=Laura)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE attendance;
TRUNCATE TABLE course_registration;
TRUNCATE TABLE course;
SET FOREIGN_KEY_CHECKS = 1;

-- 20 courses over the next 2 weeks
INSERT INTO course (coach_id, name, description, date_time, duration_minutes, capacity, enrolled_count, level, room, status) VALUES
    -- Week 1 courses
    (11, 'Morning Strength',       'Full-body strength training',      NOW() + INTERVAL 1  DAY + INTERVAL  9 HOUR, 60, 15, 8,  'INTERMEDIATE', 'Weights Room', 'SCHEDULED'),
    (12, 'Sunrise Yoga',           'Gentle morning yoga flow',         NOW() + INTERVAL 1  DAY + INTERVAL  7 HOUR, 45, 12, 12, 'BEGINNER',     'Studio A',     'SCHEDULED'),
    (13, 'HIIT Blast',             'High-intensity interval training',  NOW() + INTERVAL 2  DAY + INTERVAL 18 HOUR, 45, 20, 5,  'ADVANCED',     'Main Floor',   'SCHEDULED'),
    (11, 'Core & Stability',       'Core strength and stability work',  NOW() + INTERVAL 2  DAY + INTERVAL 10 HOUR, 50, 10, 7,  'BEGINNER',     'Studio B',     'SCHEDULED'),
    (12, 'Flexibility Flow',       'Stretch and mobility session',      NOW() + INTERVAL 3  DAY + INTERVAL  8 HOUR, 60, 15, 3,  'BEGINNER',     'Studio A',     'SCHEDULED'),
    (13, 'Cardio Circuit',         '30-minute circuit training',        NOW() + INTERVAL 3  DAY + INTERVAL 12 HOUR, 30, 25, 18, 'INTERMEDIATE', 'Main Floor',   'SCHEDULED'),
    (11, 'Power Lifting Basics',   'Introduction to power lifting',     NOW() + INTERVAL 4  DAY + INTERVAL 11 HOUR, 75, 8,  4,  'BEGINNER',     'Weights Room', 'SCHEDULED'),
    (12, 'Mindful Movement',       'Yoga nidra and meditation',        NOW() + INTERVAL 4  DAY + INTERVAL 17 HOUR, 60, 10, 6,  'BEGINNER',     'Studio A',     'SCHEDULED'),
    (13, 'Rowing Machine Mastery', 'Technique and endurance on row',   NOW() + INTERVAL 5  DAY + INTERVAL  9 HOUR, 45, 8,  8,  'INTERMEDIATE', 'Cardio Room',  'SCHEDULED'),
    (11, 'Weekend Warriors',       'Intense full-body weekend class',   NOW() + INTERVAL 6  DAY + INTERVAL 10 HOUR, 90, 20, 11, 'ADVANCED',     'Main Floor',   'SCHEDULED'),
    -- Week 2 courses
    (12, 'Yoga Fundamentals',      'Beginner-friendly yoga class',     NOW() + INTERVAL 8  DAY + INTERVAL  8 HOUR, 60, 15, 2,  'BEGINNER',     'Studio A',     'SCHEDULED'),
    (13, 'HIIT Advanced',          'For experienced HIIT athletes',    NOW() + INTERVAL 8  DAY + INTERVAL 19 HOUR, 50, 12, 9,  'ADVANCED',     'Main Floor',   'SCHEDULED'),
    (11, 'Strength Circuit',       'Multi-station strength circuit',   NOW() + INTERVAL 9  DAY + INTERVAL 11 HOUR, 60, 16, 5,  'INTERMEDIATE', 'Weights Room', 'SCHEDULED'),
    (12, 'Pilates Core',           'Pilates for core strength',        NOW() + INTERVAL 9  DAY + INTERVAL  9 HOUR, 55, 10, 10, 'INTERMEDIATE', 'Studio B',     'SCHEDULED'),
    (13, 'Speed & Agility',        'Athletic speed training',          NOW() + INTERVAL 10 DAY + INTERVAL 17 HOUR, 45, 15, 3,  'ADVANCED',     'Main Floor',   'SCHEDULED'),
    (11, 'Olympic Lifting Intro',  'Introduction to Olympic lifts',    NOW() + INTERVAL 11 DAY + INTERVAL 10 HOUR, 75, 6,  6,  'INTERMEDIATE', 'Weights Room', 'SCHEDULED'),
    (12, 'Evening Wind Down',      'Gentle yoga to end the day',       NOW() + INTERVAL 11 DAY + INTERVAL 20 HOUR, 45, 20, 14, 'BEGINNER',     'Studio A',     'SCHEDULED'),
    (13, 'Bike & Burn',            'Cycling and bodyweight combo',     NOW() + INTERVAL 12 DAY + INTERVAL  7 HOUR, 40, 10, 7,  'INTERMEDIATE', 'Cardio Room',  'SCHEDULED'),
    (11, 'Functional Fitness',     'Real-world movement patterns',     NOW() + INTERVAL 13 DAY + INTERVAL 12 HOUR, 60, 18, 0,  'BEGINNER',     'Main Floor',   'SCHEDULED'),
    (12, 'Restorative Yoga',       'Deep rest and recovery session',   NOW() + INTERVAL 14 DAY + INTERVAL 16 HOUR, 75, 12, 4,  'BEGINNER',     'Studio A',     'SCHEDULED');

ALTER TABLE course AUTO_INCREMENT = 100;

-- Registrations (member_id = auth.app_user.id)
INSERT INTO course_registration (course_id, member_id, member_email, status, registered_at) VALUES
    (1, 1,  'member1@veltro.gym', 'BOOKED', NOW() - INTERVAL 2 DAY),
    (1, 2,  'member2@veltro.gym', 'BOOKED', NOW() - INTERVAL 1 DAY),
    (2, 1,  'member1@veltro.gym', 'BOOKED', NOW() - INTERVAL 2 DAY),
    (2, 3,  'member3@veltro.gym', 'BOOKED', NOW() - INTERVAL 3 DAY),
    (3, 4,  'member4@veltro.gym', 'BOOKED', NOW() - INTERVAL 1 DAY),
    (3, 5,  'member5@veltro.gym', 'BOOKED', NOW() - INTERVAL 2 DAY),
    (4, 2,  'member2@veltro.gym', 'BOOKED', NOW() - INTERVAL 1 DAY),
    (5, 1,  'member1@veltro.gym', 'BOOKED', NOW() - INTERVAL 3 DAY),
    (6, 3,  'member3@veltro.gym', 'BOOKED', NOW() - INTERVAL 4 DAY),
    (6, 4,  'member4@veltro.gym', 'BOOKED', NOW() - INTERVAL 2 DAY),
    (7, 5,  'member5@veltro.gym', 'WAITLISTED', NOW() - INTERVAL 1 DAY),
    (8, 7,  'member7@veltro.gym', 'BOOKED', NOW() - INTERVAL 2 DAY),
    (9, 8,  'member8@veltro.gym', 'BOOKED', NOW() - INTERVAL 1 DAY),
    (10, 1, 'member1@veltro.gym', 'BOOKED', NOW() - INTERVAL 5 DAY),
    (10, 2, 'member2@veltro.gym', 'BOOKED', NOW() - INTERVAL 3 DAY);

ALTER TABLE course_registration AUTO_INCREMENT = 100;
