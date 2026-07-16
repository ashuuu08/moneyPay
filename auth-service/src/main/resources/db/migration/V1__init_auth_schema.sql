-- V1__init_auth_schema.sql
-- Initial schema for auth-service

CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(100)        NOT NULL,
    email         VARCHAR(150)        NOT NULL UNIQUE,
    phone         VARCHAR(20)         UNIQUE,
    password_hash VARCHAR(255)        NOT NULL,
    role          VARCHAR(20)         NOT NULL DEFAULT 'CUSTOMER',
    status        VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512)    NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ     NOT NULL,
    revoked     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token   ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_users_email            ON users(email);
