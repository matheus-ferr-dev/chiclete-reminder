ALTER TABLE users
    ADD COLUMN whatsapp VARCHAR(20),
    ADD COLUMN notify_email BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notify_whatsapp BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN notify_in_app BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE reminders
    ADD COLUMN last_notified_at TIMESTAMP;

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reminder_id BIGINT NOT NULL REFERENCES reminders (id) ON DELETE CASCADE,
    message VARCHAR(500) NOT NULL,
    priority_at_send VARCHAR(50) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_unread ON notifications (user_id, read, created_at DESC);
