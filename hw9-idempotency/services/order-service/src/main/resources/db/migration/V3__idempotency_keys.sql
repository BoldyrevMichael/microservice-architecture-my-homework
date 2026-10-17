-- ключ идемпотентности создания заказа: один ключ пользователя — один заказ
CREATE TABLE idempotency_keys (
    user_id      BIGINT NOT NULL,
    key          VARCHAR(128) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    order_id     BIGINT NOT NULL REFERENCES orders (id),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, key)
);
