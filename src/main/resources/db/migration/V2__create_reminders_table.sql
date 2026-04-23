CREATE TABLE reminders (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    scheduled_at TIMESTAMP NOT NULL,
    priority VARCHAR(50) NOT NULL,
    chewing BOOLEAN NOT NULL DEFAULT FALSE,
    interval_minutes INTEGER,
    ignore_count INTEGER NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);
