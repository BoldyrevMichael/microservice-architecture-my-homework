CREATE TABLE products (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(256) NOT NULL,
    available INTEGER NOT NULL CHECK (available >= 0)
);

-- резервы по заказам: по ним снимаем остаток и возвращаем при откате
CREATE TABLE reservations (
    order_id   BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products (id),
    quantity   INTEGER NOT NULL
);

-- стартовые остатки для стенда: второй товар закончился
INSERT INTO products (name, available) VALUES ('Сэндвич BLT', 100), ('Лимонад', 0);
