CREATE TABLE reservation_occupied_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    slot_start_at DATETIME NOT NULL,
    reservation_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uk_resource_slot (resource_id, slot_start_at),
    KEY idx_reservation_id (reservation_id)
);

CREATE INDEX idx_reservation_overlap ON reservations (resource_id, start_at, end_at, status, deleted);
