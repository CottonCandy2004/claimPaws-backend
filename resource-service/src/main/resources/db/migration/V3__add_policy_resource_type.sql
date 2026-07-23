ALTER TABLE reservation_policies ADD COLUMN resource_type VARCHAR(20) DEFAULT 'MEETING_ROOM' AFTER name;
