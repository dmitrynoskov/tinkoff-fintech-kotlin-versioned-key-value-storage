--liquibase formatted sql

--changeset Dmitry:1
CREATE TABLE user_info
(
    user_id      SERIAL PRIMARY KEY,
    phone_number VARCHAR(12)  NOT NULL,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL,
    UNIQUE (phone_number)
);

--changeset Dmitry:2
CREATE TABLE record
(
    record_id   SERIAL PRIMARY KEY,
    user_id     INT          NOT NULL REFERENCES user_info ON DELETE CASCADE,
    key         VARCHAR(100) NOT NULL,
    value       TEXT         NOT NULL,
    revision    INT DEFAULT 0,
    update_time TIMESTAMP    NOT NULL,
    UNIQUE (user_id, key, revision)
);
