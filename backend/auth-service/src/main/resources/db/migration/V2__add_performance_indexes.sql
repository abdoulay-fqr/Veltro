-- V2__add_performance_indexes.sql
-- Auth Service — performance indexes for high-frequency lookup paths

-- token_blacklist: existsByToken is called on every authenticated request
-- The UNIQUE constraint on token already creates an index; this explicitly
-- names it for clarity and adds a composite for TTL-based cleanup queries.
CREATE INDEX idx_token_blacklist_at
    ON token_blacklist (blacklisted_at);

-- refresh_token: findByToken + expiry cleanup queries
CREATE INDEX idx_refresh_token_expires
    ON refresh_token (expires_at);

-- app_user: role-based filtering for admin tooling
CREATE INDEX idx_app_user_role
    ON app_user (role);
