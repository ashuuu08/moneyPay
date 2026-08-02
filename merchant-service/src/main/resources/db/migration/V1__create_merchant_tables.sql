-- Merchant service DB schema (Phase 3)

CREATE TABLE IF NOT EXISTS merchants (
    id               UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id          UUID         NOT NULL UNIQUE,
    business_name    VARCHAR(200) NOT NULL,
    business_email   VARCHAR(150) NOT NULL UNIQUE,
    business_phone   VARCHAR(20),
    gstin            VARCHAR(20),
    pan              VARCHAR(10),
    kyc_status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    webhook_url      VARCHAR(500),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ,

    CONSTRAINT chk_kyc_status    CHECK (kyc_status IN ('PENDING','UNDER_REVIEW','APPROVED','REJECTED')),
    CONSTRAINT chk_merchant_status CHECK (status IN ('ACTIVE','SUSPENDED','CLOSED'))
);

CREATE TABLE IF NOT EXISTS merchant_api_keys (
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    merchant_id UUID         NOT NULL REFERENCES merchants(id),
    key_prefix  VARCHAR(20)  NOT NULL UNIQUE,
    key_hash    TEXT         NOT NULL,
    label       VARCHAR(100),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_merchant_user_id    ON merchants (user_id);
CREATE INDEX IF NOT EXISTS idx_merchant_kyc_status ON merchants (kyc_status);
CREATE INDEX IF NOT EXISTS idx_api_key_merchant_id ON merchant_api_keys (merchant_id);
CREATE INDEX IF NOT EXISTS idx_api_key_prefix      ON merchant_api_keys (key_prefix);
