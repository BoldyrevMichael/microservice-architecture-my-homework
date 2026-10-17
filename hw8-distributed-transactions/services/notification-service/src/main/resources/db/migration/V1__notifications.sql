-- адреса получателей приходят событием регистрации; письма не отправляются, а сохраняются
CREATE TABLE recipients (
    user_id BIGINT PRIMARY KEY,
    email   VARCHAR(256)
);

CREATE TABLE messages (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    email      VARCHAR(256),
    subject    VARCHAR(256) NOT NULL,
    body       TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX messages_user_id_idx ON messages (user_id);
