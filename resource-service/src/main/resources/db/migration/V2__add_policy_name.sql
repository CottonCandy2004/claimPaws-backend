ALTER TABLE reservation_policies ADD COLUMN name VARCHAR(100) AFTER resource_id;
ALTER TABLE reservation_policies ADD COLUMN description VARCHAR(500) AFTER approval_level;
