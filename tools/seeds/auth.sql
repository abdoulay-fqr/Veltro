-- ============================================================
-- Veltro Auth Seed — veltro_auth database
-- Password for ALL accounts: Veltro@2024
-- BCrypt(10) hash pre-computed
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE token_blacklist;
TRUNCATE TABLE refresh_token;
TRUNCATE TABLE app_user;
SET FOREIGN_KEY_CHECKS = 1;

-- BCrypt(10) hash of "Veltro@2024"
-- Regenerate with: new BCryptPasswordEncoder(10).encode("Veltro@2024")
SET @pwd = '$2a$10$kl98sJu9.yJXuhzHApqEE.3tDU0xgmRh5bfHN57z3FdW6YYyE2ywO';

-- Members (IDs 1-10)
INSERT INTO app_user (id, identifier, password, role) VALUES
    (1,  'member1@veltro.gym',  @pwd, 'MEMBER'),
    (2,  'member2@veltro.gym',  @pwd, 'MEMBER'),
    (3,  'member3@veltro.gym',  @pwd, 'MEMBER'),
    (4,  'member4@veltro.gym',  @pwd, 'MEMBER'),
    (5,  'member5@veltro.gym',  @pwd, 'MEMBER'),
    (6,  'member6@veltro.gym',  @pwd, 'MEMBER'),
    (7,  'member7@veltro.gym',  @pwd, 'MEMBER'),
    (8,  'member8@veltro.gym',  @pwd, 'MEMBER'),
    (9,  'member9@veltro.gym',  @pwd, 'MEMBER'),
    (10, 'member10@veltro.gym', @pwd, 'MEMBER'),
    -- Coaches (IDs 11-13)
    (11, 'coach1@veltro.gym',   @pwd, 'COACH'),
    (12, 'coach2@veltro.gym',   @pwd, 'COACH'),
    (13, 'coach3@veltro.gym',   @pwd, 'COACH'),
    -- Admin (ID 14)
    (14, 'admin@veltro.gym',    @pwd, 'ADMIN');

-- Reset auto_increment past seed IDs
ALTER TABLE app_user AUTO_INCREMENT = 100;
