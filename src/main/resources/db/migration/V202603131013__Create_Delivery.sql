CREATE TYPE delivery_company AS ENUM ('SF', 'JD', 'YTO', 'ZTO');

CREATE TYPE delivery_status AS ENUM ('PENDING', 'PUSHED', 'DELIVERED', 'ARRIVED', 'CANCELED');

CREATE TYPE shipping_type AS ENUM ('LOGISTICS', 'PERSONAL');

CREATE TYPE shipping_status AS ENUM ('PENDING', 'SHIPPED', 'ARRIVED', 'CANCELED');

CREATE TYPE payment_method AS ENUM ('WECHAT', 'ALIPAY', 'JDPAY', 'UNIONPAY', 'CASH');

CREATE TABLE delivery
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT              NOT NULL,
    name            VARCHAR(20)         NOT NULL,
    phone           VARCHAR(11)         NOT NULL,
    address         TEXT                NOT NULL,
    company         delivery_company    NOT NULL,
    tracking_number VARCHAR(50)         NOT NULL,
    status          delivery_status     NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    comment         TEXT,
    task_id         VARCHAR(255) UNIQUE NOT NULL,
    order_id        VARCHAR(255),
    query_token     VARCHAR(255) UNIQUE,
    CONSTRAINT fk_delivery_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE TABLE delivery_order
(
    delivery_id BIGINT NOT NULL,
    order_id    BIGINT NOT NULL,
    PRIMARY KEY (delivery_id, order_id),
    CONSTRAINT fk_delivery_order_delivery FOREIGN KEY (delivery_id) REFERENCES delivery (id) ON DELETE NO ACTION,
    CONSTRAINT fk_delivery_order_order FOREIGN KEY (order_id) REFERENCES "order" (id) ON DELETE CASCADE
);

CREATE TABLE shipping
(
    id              BIGSERIAL PRIMARY KEY,
    type            shipping_type   NOT NULL DEFAULT 'LOGISTICS',
    carrier         VARCHAR(50)     NOT NULL,
    tracking_number VARCHAR(50)     NOT NULL,
    tax             DECIMAL(10, 2),
    fee             DECIMAL(10, 2),
    status          shipping_status NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    comment         TEXT
);

ALTER TABLE "order" ADD COLUMN shipping_id BIGINT;

ALTER TABLE "order" ADD CONSTRAINT fk_order_shipping FOREIGN KEY (shipping_id) REFERENCES shipping (id) ON DELETE SET NULL;

ALTER TABLE "order" ADD COLUMN comment TEXT;
