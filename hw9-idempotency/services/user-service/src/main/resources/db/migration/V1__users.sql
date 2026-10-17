CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(256) NOT NULL UNIQUE,
    first_name VARCHAR(256),
    last_name  VARCHAR(256),
    email      VARCHAR(256),
    phone      VARCHAR(32)
);
