SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'member_profile'
      AND COLUMN_NAME  = 'email'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE member_profile ADD COLUMN email VARCHAR(255) NULL AFTER user_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
