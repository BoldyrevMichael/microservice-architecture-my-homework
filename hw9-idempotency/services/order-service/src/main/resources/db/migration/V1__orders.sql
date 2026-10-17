CREATE TABLE orders (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    price      NUMERIC(12, 2) NOT NULL,
    status     VARCHAR(20) NOT NULL,
    reason     VARCHAR(256),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX orders_user_id_idx ON orders (user_id);
