CREATE INDEX idx_transactions_account_time
    ON transactions (account_id, timestamp);
