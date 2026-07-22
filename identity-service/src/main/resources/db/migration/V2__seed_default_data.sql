INSERT INTO roles (name, description) VALUES
    ('admin', '系统管理员'),
    ('user', '普通用户');

INSERT INTO users (username, password_hash, display_name, enabled) VALUES
    ('admin', '$2a$10$2Cs1a9wSKajp55/O4SVKpujahXMT84AnE4ZmRyHpFbYjoDBltxVSe', '管理员', TRUE);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r WHERE u.username = 'admin' AND r.name = 'admin';
