CREATE TABLE accounts (
    user_id BIGINT PRIMARY KEY,
    balance NUMERIC(12, 2) NOT NULL DEFAULT 0
);
