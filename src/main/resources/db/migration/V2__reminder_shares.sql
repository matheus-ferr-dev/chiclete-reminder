CREATE TABLE reminder_shares (
    reminder_id BIGINT NOT NULL REFERENCES reminders (id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (reminder_id, user_id)
);

CREATE INDEX idx_reminder_shares_user ON reminder_shares (user_id);
