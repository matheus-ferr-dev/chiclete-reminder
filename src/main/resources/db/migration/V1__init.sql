CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE group_members (
    group_id BIGINT NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (group_id, user_id)
);

CREATE TABLE reminders (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    scheduled_at TIMESTAMP NOT NULL,
    chewing BOOLEAN NOT NULL DEFAULT FALSE,
    interval_minutes INTEGER,
    ignore_count INTEGER NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    priority VARCHAR(50) NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_reminders_owner ON reminders (owner_id);
