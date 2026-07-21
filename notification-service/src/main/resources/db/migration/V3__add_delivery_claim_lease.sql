ALTER TABLE notification_deliveries
    ADD COLUMN claimed_at TIMESTAMP NULL AFTER next_attempt_at,
    ADD INDEX idx_delivery_claim_lease (status, claimed_at, deleted);
