ALTER TABLE roles ADD COLUMN code VARCHAR(50) AFTER name;
UPDATE roles SET code = name WHERE code IS NULL;
