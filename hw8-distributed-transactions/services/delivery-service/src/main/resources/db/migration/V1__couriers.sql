-- резервы курьеров: на стенде один курьер, поэтому слот может быть занят только одним заказом
CREATE TABLE courier_reservations (
    order_id BIGINT PRIMARY KEY,
    user_id  BIGINT NOT NULL,
    slot     VARCHAR(32) NOT NULL UNIQUE
);
