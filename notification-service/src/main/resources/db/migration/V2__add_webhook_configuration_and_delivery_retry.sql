CREATE TABLE webhook_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    endpoint_url VARCHAR(2048) NOT NULL,
    encrypted_secret TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_webhook_configs_active (enabled, deleted)
);

ALTER TABLE notification_deliveries
    ADD COLUMN webhook_config_id BIGINT NULL AFTER id,
    ADD COLUMN endpoint_url VARCHAR(2048) NULL AFTER event_type,
    ADD COLUMN next_attempt_at TIMESTAMP NULL AFTER last_attempt_at,
    ADD COLUMN response_status INT NULL AFTER next_attempt_at,
    ADD COLUMN failure_reason VARCHAR(500) NULL AFTER response_status,
    ADD INDEX idx_delivery_due (status, next_attempt_at, deleted);
