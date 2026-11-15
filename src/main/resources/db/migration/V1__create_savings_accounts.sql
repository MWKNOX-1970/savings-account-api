CREATE TABLE savings_accounts (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_number    VARCHAR(12)  NOT NULL,
    customer_name     VARCHAR(100) NOT NULL,
    account_nick_name VARCHAR(30),
    CONSTRAINT uq_savings_accounts_account_number UNIQUE (account_number)
);

-- Supports the "max 5 accounts per customer" count.
CREATE INDEX idx_savings_accounts_customer_name ON savings_accounts (UPPER(customer_name));
