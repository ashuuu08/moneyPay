-- Wallet tables for wallet-service (Phase 2)

CREATE TABLE IF NOT EXISTS wallets (
    id          UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id     UUID           NOT NULL,
    currency    VARCHAR(10)    NOT NULL,
    balance     NUMERIC(19, 4) NOT NULL DEFAULT 0,
    version     BIGINT         NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ,

    CONSTRAINT uq_wallets_user_currency UNIQUE (user_id, currency),
    CONSTRAINT chk_wallets_balance CHECK (balance >= 0)
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id            UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    wallet_id     UUID           NOT NULL REFERENCES wallets(id),
    type          VARCHAR(10)    NOT NULL,
    amount        NUMERIC(19, 4) NOT NULL,
    reference_id  VARCHAR(100),
    description   VARCHAR(255),
    balance_after NUMERIC(19, 4) NOT NULL,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_wallet_tx_type   CHECK (type IN ('CREDIT', 'DEBIT')),
    CONSTRAINT chk_wallet_tx_amount CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_wallet_tx_wallet_id    ON wallet_transactions (wallet_id);
CREATE INDEX IF NOT EXISTS idx_wallet_tx_reference_id ON wallet_transactions (reference_id);
CREATE INDEX IF NOT EXISTS idx_wallet_tx_created_at   ON wallet_transactions (created_at DESC);
