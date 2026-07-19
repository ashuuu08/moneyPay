-- Ledger entries table for ledger-service (Phase 2)
-- Implements double-entry bookkeeping for all financial mutations.

CREATE TABLE IF NOT EXISTS ledger_entries (
    id          UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    payment_id  UUID           NOT NULL,
    account_id  UUID           NOT NULL,
    entry_type  VARCHAR(10)    NOT NULL,
    amount      NUMERIC(19, 4) NOT NULL,
    currency    VARCHAR(10)    NOT NULL,
    description VARCHAR(255),
    posted_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_ledger_entry_type   CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_ledger_amount       CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_ledger_payment_id ON ledger_entries (payment_id);
CREATE INDEX IF NOT EXISTS idx_ledger_account_id ON ledger_entries (account_id);
CREATE INDEX IF NOT EXISTS idx_ledger_posted_at  ON ledger_entries (posted_at DESC);

-- Each payment should have exactly one DEBIT and one CREDIT entry (double-entry invariant)
CREATE UNIQUE INDEX IF NOT EXISTS uq_ledger_payment_entry_type
    ON ledger_entries (payment_id, entry_type);
