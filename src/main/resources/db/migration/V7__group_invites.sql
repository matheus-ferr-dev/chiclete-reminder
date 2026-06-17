ALTER TABLE groups
    ADD COLUMN owner_id BIGINT REFERENCES users (id);

UPDATE groups g
SET owner_id = (
    SELECT gm.user_id
    FROM group_members gm
    WHERE gm.group_id = g.id
    ORDER BY gm.user_id
    LIMIT 1
)
WHERE owner_id IS NULL;

CREATE TABLE group_invites (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    invited_user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    invited_by_user_id BIGINT NOT NULL REFERENCES users (id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (group_id, invited_user_id)
);

CREATE INDEX idx_group_invites_user_status ON group_invites (invited_user_id, status);

ALTER TABLE notifications
    ALTER COLUMN reminder_id DROP NOT NULL;

ALTER TABLE notifications
    ADD COLUMN type VARCHAR(30) NOT NULL DEFAULT 'REMINDER',
    ADD COLUMN group_invite_id BIGINT REFERENCES group_invites (id) ON DELETE CASCADE;

CREATE INDEX idx_notifications_type ON notifications (user_id, type, read, created_at DESC);
