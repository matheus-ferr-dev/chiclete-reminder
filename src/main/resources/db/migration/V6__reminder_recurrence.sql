ALTER TABLE reminders
    ADD COLUMN recurrence_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
    ADD COLUMN recurrence_days VARCHAR(32),
    ADD COLUMN recurring_enabled BOOLEAN NOT NULL DEFAULT TRUE;
