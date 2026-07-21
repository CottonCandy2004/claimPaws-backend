INSERT INTO resources (id, name, type, floor, building, capacity, description, active, created_at, updated_at, deleted)
VALUES (1, '会议室A', 'MEETING_ROOM', '3', 'A栋', 10, '标准会议室', TRUE, NOW(), NOW(), FALSE);

INSERT INTO resources (id, name, type, floor, building, capacity, description, active, created_at, updated_at, deleted)
VALUES (2, '会议室B', 'MEETING_ROOM', '5', 'B栋', 8, '小型会议室', TRUE, NOW(), NOW(), FALSE);

INSERT INTO reservation_policies (id, resource_id, slot_minutes, advance_days, min_duration_minutes, max_duration_minutes, cancel_deadline_minutes, check_in_window_minutes, requires_approval, approval_level, active, created_at, updated_at, deleted)
VALUES (1, 1, 30, 30, 30, 480, 60, 15, FALSE, 0, TRUE, NOW(), NOW(), FALSE);
