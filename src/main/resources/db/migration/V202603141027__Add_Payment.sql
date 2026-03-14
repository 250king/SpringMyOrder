CREATE TYPE payment_type AS ENUM ('LIST', 'SHIPPING', 'DELIVERY', 'TAX');

CREATE TABLE payment
(
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT         NOT NULL,
    type           payment_type   NOT NULL,
    rereference_id BIGINT         NOT NULL,
    amount         DECIMAL(10, 2) NOT NULL,
    refund_amount  DECIMAL(10, 2) NOT NULL DEFAULT 0,
    currency       VARCHAR(3)     NOT NULL DEFAULT 'CNY',
    currency_rate  DECIMAL(10, 4) NOT NULL DEFAULT 1,
    method         payment_method NOT NULL,
    created_at     timestamptz    NOT NULL DEFAULT NOW(),
    updated_at     timestamptz    NOT NULL DEFAULT NOW(),
    paid_at        timestamptz,
    request_id     VARCHAR(255) UNIQUE
);

ALTER TYPE order_status RENAME TO list_status;
ALTER TYPE list_status ADD VALUE 'CONFIRMED' AFTER 'PENDING';
ALTER TYPE list_status ADD VALUE 'RESERVED' AFTER 'ARRIVED';

ALTER TABLE cart RENAME TO list;
ALTER TABLE list DROP CONSTRAINT cart_pkey;
ALTER TABLE list ADD COLUMN id BIGSERIAL PRIMARY KEY;
ALTER TABLE list ADD COLUMN shipping_id BIGINT REFERENCES shipping(id);
ALTER TABLE list ADD COLUMN status list_status NOT NULL DEFAULT 'PENDING';
ALTER TABLE list RENAME CONSTRAINT cart_quantity_check TO list_quantity_check;
ALTER TABLE list RENAME CONSTRAINT fk_cart_item TO fk_list_item;
ALTER TABLE list RENAME CONSTRAINT fk_cart_user TO fk_list_user;

ALTER TABLE delivery_order RENAME TO delivery_list;
ALTER TABLE delivery_list RENAME COLUMN order_id TO list_id;
ALTER TABLE delivery_list DROP CONSTRAINT fk_delivery_order_order;
ALTER TABLE delivery_list ADD CONSTRAINT fk_delivery_list_list FOREIGN KEY (list_id) REFERENCES list(id);
ALTER TABLE delivery_list RENAME CONSTRAINT fk_delivery_order_delivery TO fk_delivery_list_delivery;
ALTER TABLE delivery_list RENAME CONSTRAINT delivery_order_pkey TO delivery_list_pkey;
DROP TABLE "order";

ALTER TABLE delivery ALTER COLUMN name DROP NOT NULL;
ALTER TABLE delivery ALTER COLUMN phone DROP NOT NULL;
ALTER TABLE delivery ALTER COLUMN address DROP NOT NULL;
