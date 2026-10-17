-- данные для саги: товар, количество, слот доставки; статусы шагов
ALTER TABLE orders ADD COLUMN product_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE orders ADD COLUMN quantity   INTEGER NOT NULL DEFAULT 1;
ALTER TABLE orders ADD COLUMN slot       VARCHAR(32) NOT NULL DEFAULT '';
ALTER TABLE orders ALTER COLUMN product_id DROP DEFAULT;
ALTER TABLE orders ALTER COLUMN quantity DROP DEFAULT;
ALTER TABLE orders ALTER COLUMN slot DROP DEFAULT;
