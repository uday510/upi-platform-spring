-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================
-- ACCOUNTS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    upi_id VARCHAR(255) NOT NULL UNIQUE,

    balance NUMERIC(19,4) NOT NULL CHECK (balance >= 0),

    version BIGINT NOT NULL DEFAULT 0
);

-- ============================================
-- TRANSACTIONS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    from_account_id UUID NOT NULL,
    to_account_id   UUID NOT NULL,

    amount NUMERIC(19,4) NOT NULL CHECK (amount > 0),

    status VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),

    idempotency_key VARCHAR(255) NOT NULL UNIQUE,

    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_tx_from
        FOREIGN KEY (from_account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_tx_to
        FOREIGN KEY (to_account_id)
        REFERENCES accounts(id),

    CONSTRAINT chk_not_self
        CHECK (from_account_id <> to_account_id)
);

-- Indexes for fast lookups
CREATE INDEX IF NOT EXISTS idx_tx_from
    ON transactions(from_account_id);

CREATE INDEX IF NOT EXISTS idx_tx_to
    ON transactions(to_account_id);

-- ============================================
-- LEDGER ENTRIES TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    account_id     UUID NOT NULL,
    transaction_id UUID NOT NULL,

    type VARCHAR(20) NOT NULL
        CHECK (type IN ('DEBIT', 'CREDIT')),

    amount NUMERIC(19,4) NOT NULL CHECK (amount > 0),

    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_ledger_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_ledger_tx
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_ledger_account
    ON ledger_entries(account_id);

CREATE INDEX IF NOT EXISTS idx_ledger_tx
    ON ledger_entries(transaction_id);

-- Prevent duplicate debit/credit per transaction
CREATE UNIQUE INDEX IF NOT EXISTS uq_ledger_unique
    ON ledger_entries(transaction_id, type);
