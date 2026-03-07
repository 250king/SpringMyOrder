CREATE TYPE group_status AS ENUM ('OPENING', 'CLOSED', 'FINISHED');

CREATE TYPE "role" AS ENUM ('OWNER', 'ADMIN', 'MEMBER');

CREATE TYPE order_status AS ENUM ('PENDING', 'PAID', 'DEPARTED', 'IN_DEPOT', 'SHIPPED', 'ARRIVED', 'DELIVERED', 'FINISHED', 'CANCELLED');

CREATE TABLE "user"
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(20) NOT NULL UNIQUE,
    qq           VARCHAR(32) NOT NULL UNIQUE,
    email        VARCHAR(255) UNIQUE,
    credit_score INT         NOT NULL DEFAULT 100 CHECK (credit_score >= 0 AND credit_score <= 100),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMPTZ
);

CREATE TABLE "group"
(
    id         BIGSERIAL PRIMARY KEY,
    owner_id   BIGINT       NOT NULL,
    name       VARCHAR(20)  NOT NULL UNIQUE,
    qq         VARCHAR(20)  NOT NULL UNIQUE,
    status     group_status NOT NULL DEFAULT 'OPENING',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_group_user FOREIGN KEY (owner_id) REFERENCES "user" (id) ON DELETE NO ACTION
);

CREATE TABLE address
(
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT      NOT NULL,
    name    VARCHAR(20) NOT NULL,
    phone   VARCHAR(11) NOT NULL,
    address TEXT        NOT NULL,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE TABLE group_user
(
    user_id    BIGINT      NOT NULL,
    group_id   BIGINT      NOT NULL,
    role       "role"      NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_group_user_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_group_user_group FOREIGN KEY (group_id) REFERENCES "group" (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, group_id)
);

CREATE TABLE item
(
    id         BIGSERIAL PRIMARY KEY,
    group_id   BIGINT         NOT NULL,
    creator_id BIGINT         NOT NULL,
    name       VARCHAR(100)    NOT NULL,
    url        VARCHAR(2048)  NOT NULL,
    image      VARCHAR(2048),
    price      DECIMAL(10, 2) NOT NULL,
    weight     INT CHECK ( weight >= 0 ),
    is_allowed BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_group FOREIGN KEY (group_id) REFERENCES "group" (id) ON DELETE NO ACTION,
    CONSTRAINT fk_item_user FOREIGN KEY (creator_id) REFERENCES "user" (id) ON DELETE NO ACTION,
    UNIQUE (group_id, name),
    UNIQUE (group_id, url)
);

CREATE TABLE cart
(
    user_id    BIGINT      NOT NULL,
    item_id    BIGINT      NOT NULL,
    quantity   INT         NOT NULL DEFAULT 1 CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item FOREIGN KEY (item_id) REFERENCES item (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, item_id)
);

CREATE TABLE "order"
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT         NOT NULL,
    item_id    BIGINT,
    name       VARCHAR(100)    NOT NULL,
    url        VARCHAR(2048)  NOT NULL,
    image      VARCHAR(2048),
    price      DECIMAL(10, 2) NOT NULL,
    weight     INT CHECK ( weight >= 0 ),
    quantity   INT            NOT NULL DEFAULT 1 CHECK (quantity > 0),
    status     order_status   NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item FOREIGN KEY (item_id) REFERENCES item (id) ON DELETE SET NULL
)
