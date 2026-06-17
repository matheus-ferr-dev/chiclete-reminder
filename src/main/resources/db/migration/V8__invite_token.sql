ALTER TABLE group_invites
    ALTER COLUMN invited_user_id DROP NOT NULL;

ALTER TABLE group_invites
    ADD COLUMN invited_email VARCHAR(255),
    ADD COLUMN invite_token VARCHAR(64),
    ADD COLUMN expires_at TIMESTAMP;

UPDATE group_invites
SET invite_token = md5(random()::text || clock_timestamp()::text || id::text)
WHERE invite_token IS NULL;

ALTER TABLE group_invites
    ALTER COLUMN invite_token SET NOT NULL;

ALTER TABLE group_invites
    DROP CONSTRAINT IF EXISTS group_invites_group_id_invited_user_id_key;

CREATE UNIQUE INDEX idx_group_invites_token ON group_invites (invite_token);
CREATE UNIQUE INDEX idx_group_invites_group_user_pending
    ON group_invites (group_id, invited_user_id)
    WHERE invited_user_id IS NOT NULL AND status = 'PENDING';
CREATE UNIQUE INDEX idx_group_invites_group_email_pending
    ON group_invites (group_id, lower(invited_email))
    WHERE invited_email IS NOT NULL AND status = 'PENDING';
