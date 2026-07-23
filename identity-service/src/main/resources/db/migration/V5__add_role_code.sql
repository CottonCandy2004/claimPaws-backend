ALTER TABLE roles ADD COLUMN code VARCHAR(50) AFTER name;
UPDATE roles SET code = 'ADMIN' WHERE name = 'admin' AND code IS NULL;
UPDATE roles SET code = 'USER' WHERE name = 'user' AND code IS NULL;
