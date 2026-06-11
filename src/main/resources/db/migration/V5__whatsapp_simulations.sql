CREATE TABLE whatsapp_simulations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    reminder_id BIGINT NOT NULL REFERENCES reminders (id) ON DELETE CASCADE,
    phone VARCHAR(20) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_whatsapp_sim_user ON whatsapp_simulations (user_id, created_at DESC);
