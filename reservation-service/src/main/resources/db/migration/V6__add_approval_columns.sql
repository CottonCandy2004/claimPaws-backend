ALTER TABLE reservations ADD COLUMN approver_roles VARCHAR(500) DEFAULT '' AFTER approval_level;
ALTER TABLE reservations ADD COLUMN approved_levels INT DEFAULT 0 AFTER approver_roles;
