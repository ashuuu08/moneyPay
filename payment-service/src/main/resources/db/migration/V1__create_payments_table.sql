-- Payments table for payment-service (Phase 2)
CREATE TABLE IF NOT EXISTS payments (
    id               UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id         VARCHAR(100) NOT NULL,
    user_id          UUID         NOT NULL,
    merchant_id      UUID         NOT NULL,
    amount           NUMERIC(19, 4) NOT NULL,
    currency         VARCHAR(10)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'INITIATED',
    provider         VARCHAR(50),
    provider_ref     VARCHAR(100),
    idempotency_key  VARCHAR(100) NOT NULL,
    description      VARCHAR(255),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ,

    CONSTRAINT uq_payments_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT chk_payments_status CHECK (status IN ('INITIATED','SUCCESS','FAILED','REFUNDED')),
    CONSTRAINT chk_payments_amount CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_payments_order_id       ON payments (order_id);
CREATE INDEX IF NOT EXISTS idx_payments_user_id        ON payments (user_id);
CREATE INDEX IF NOT EXISTS idx_payments_merchant_id    ON payments (merchant_id);
CREATE INDEX IF NOT EXISTS idx_payments_status         ON payments (status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at     ON payments (created_at DESC);
