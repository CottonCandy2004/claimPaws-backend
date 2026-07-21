ALTER TABLE outbox_messages
    ADD COLUMN lock_owner VARCHAR(64) NULL,
    ADD COLUMN lock_until TIMESTAMP NULL,
    ADD INDEX idx_outbox_claim (status, lock_until, created_at);
