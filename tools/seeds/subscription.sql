-- ============================================================
-- Veltro Subscription Seed — veltro_subscription database
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE payment_record;
TRUNCATE TABLE subscription;
SET FOREIGN_KEY_CHECKS = 1;

-- member_id here = auth.app_user.id (not member_profile.id)
INSERT INTO subscription (member_id, member_email, plan, status, start_date, end_date, auto_renew, paused_months_used) VALUES
    -- Active plans
    (1,  'member1@veltro.gym',  'MONTHLY', 'ACTIVE',    CURDATE() - INTERVAL 10 DAY, CURDATE() + INTERVAL 20 DAY, TRUE,  0),
    (2,  'member2@veltro.gym',  'ANNUAL',  'ACTIVE',    CURDATE() - INTERVAL 30 DAY, CURDATE() + INTERVAL 335 DAY,TRUE,  0),
    (3,  'member3@veltro.gym',  'SESSION', 'ACTIVE',    CURDATE(),                   CURDATE() + INTERVAL 1 DAY,  FALSE, 0),
    (4,  'member4@veltro.gym',  'MONTHLY', 'ACTIVE',    CURDATE() - INTERVAL 5 DAY,  CURDATE() + INTERVAL 25 DAY, FALSE, 0),
    (5,  'member5@veltro.gym',  'TRIAL',   'ACTIVE',    CURDATE() - INTERVAL 3 DAY,  CURDATE() + INTERVAL 4 DAY,  FALSE, 0),
    -- Expiring soon (for demo of SubscriptionExpiring event)
    (7,  'member7@veltro.gym',  'MONTHLY', 'ACTIVE',    CURDATE() - INTERVAL 27 DAY, CURDATE() + INTERVAL 3 DAY,  FALSE, 0),
    (8,  'member8@veltro.gym',  'TRIAL',   'ACTIVE',    CURDATE() - INTERVAL 6 DAY,  CURDATE() + INTERVAL 1 DAY,  FALSE, 0),
    -- Cancelled / Expired (for history)
    (9,  'member9@veltro.gym',  'TRIAL',   'EXPIRED',   CURDATE() - INTERVAL 14 DAY, CURDATE() - INTERVAL 7 DAY,  FALSE, 0),
    (10, 'member10@veltro.gym', 'MONTHLY', 'CANCELLED', CURDATE() - INTERVAL 20 DAY, CURDATE() + INTERVAL 10 DAY, FALSE, 0);

-- Payment records for paid subscriptions
INSERT INTO payment_record (subscription_id, amount, paid_at, method, invoice_ref) VALUES
    (1, 39.00, NOW() - INTERVAL 10 DAY, 'CARD',     'INV-A1B2C3D4'),
    (2, 374.00,NOW() - INTERVAL 30 DAY, 'CARD',     'INV-E5F6G7H8'),
    (3, 15.00, NOW(),                   'CARD',     'INV-I9J0K1L2'),
    (4, 39.00, NOW() - INTERVAL 5 DAY,  'TRANSFER', 'INV-M3N4O5P6'),
    (7, 39.00, NOW() - INTERVAL 27 DAY, 'CARD',     'INV-Q7R8S9T0');

ALTER TABLE subscription AUTO_INCREMENT = 100;
ALTER TABLE payment_record AUTO_INCREMENT = 100;
