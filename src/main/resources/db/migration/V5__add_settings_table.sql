CREATE TABLE settings (
    name VARCHAR(100) PRIMARY KEY,
    value DECIMAL(19,2) NOT NULL
);

INSERT INTO settings (name, value) VALUES ('DAILY_TRANSFER_LIMIT', 50000);
