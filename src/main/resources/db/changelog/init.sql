--liquibase formatted sql

--changeset Dmitry:1
CREATE TABLE user_info
(
    user_id      BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(12)  NOT NULL,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL,
    UNIQUE (phone_number)
);

--changeset Dmitry:2
CREATE TABLE record
(
    record_id   BIGSERIAL PRIMARY KEY,
    user_id     BIGINT          NOT NULL REFERENCES user_info ON DELETE CASCADE,
    key         VARCHAR(100) NOT NULL,
    value       TEXT         NOT NULL,
    revision    INT DEFAULT 0,
    update_time TIMESTAMP    NOT NULL,
    UNIQUE (user_id, key, revision)
);

--changeset Dmitry:3
CREATE INDEX user_phone_index ON user_info(phone_number);
CREATE INDEX record_user_index ON record(user_id);
CREATE INDEX record_key_index ON record(key);
CREATE INDEX record_update_time_index ON record(update_time);
