CREATE EXTENSION IF NOT EXISTS "pgcrypto";


CREATE TABLE IF NOT EXISTS accounts (

    id UUID PRIMARY KEY,

    upi_id VARCHAR(100) NOT NULL UNIQUE,

    balance NUMERIC(19,4) NOT NULL DEFAULT 0
        CHECK (balance >= 0),

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_accounts_upi
    ON accounts(upi_id);


CREATE TABLE IF NOT EXISTS transactions (

    id UUID PRIMARY KEY,

    from_account_id UUID NOT NULL,
    to_account_id   UUID NOT NULL,

    amount NUMERIC(19,4) NOT NULL
        CHECK (amount > 0),

    status VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDING','SUCCESS','FAILED')),

    idempotency_key VARCHAR(64) NOT NULL UNIQUE,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_tx_from
        FOREIGN KEY (from_account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_tx_to
        FOREIGN KEY (to_account_id)
        REFERENCES accounts(id),

    CONSTRAINT chk_tx_not_self
        CHECK (from_account_id <> to_account_id)
);

CREATE INDEX IF NOT EXISTS idx_tx_from_created
    ON transactions(from_account_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_tx_to_created
    ON transactions(to_account_id, created_at DESC);



CREATE TABLE IF NOT EXISTS ledger_entries (

    id UUID PRIMARY KEY,

    account_id UUID NOT NULL,

    transaction_id UUID NOT NULL,

    type VARCHAR(10) NOT NULL
        CHECK (type IN ('DEBIT','CREDIT')),

    amount NUMERIC(19,4) NOT NULL
        CHECK (amount > 0),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ledger_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_ledger_tx
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id),

    -- Prevent duplicate debit/credit per transaction
    CONSTRAINT uq_ledger_tx_type
        UNIQUE (transaction_id, type)
);

CREATE INDEX IF NOT EXISTS idx_ledger_account_created
    ON ledger_entries(account_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ledger_tx
    ON ledger_entries(transaction_id);


--CREATE OR REPLACE FUNCTION prevent_ledger_update_delete()
--RETURNS trigger AS $$
--BEGIN
--    RAISE EXCEPTION 'Ledger entries are immutable';
--END;
--$$ LANGUAGE plpgsql;
--
--DROP TRIGGER IF EXISTS trg_ledger_immutable ON ledger_entries;
--
--CREATE TRIGGER trg_ledger_immutable
--BEFORE UPDATE OR DELETE ON ledger_entries
--FOR EACH ROW
--EXECUTE FUNCTION prevent_ledger_update_delete();


CREATE OR REPLACE VIEW account_balances_from_ledger AS
SELECT
    account_id,
    COALESCE(
        SUM(
            CASE
                WHEN type = 'CREDIT' THEN amount
                ELSE -amount
            END
        ),
        0
    ) AS balance
FROM ledger_entries
GROUP BY account_id;
