INSERT INTO users (email, password, role, locked, enabled)
VALUES ('admin@acs.com',
        '$2a$10$bbSF.JLzs9zICQUtWnkl2ux5Zzgk/piP2tKJQR80QkrSLjFl.ThDy',
        'ROLE_ADMIN',
        FALSE,
        TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_info (first_name, last_name, phone_number, user_id)
SELECT 'Admin', 'Default', '07234567890', u.id
FROM users u
WHERE u.email = 'admin@acs.com'
  AND NOT EXISTS (
    SELECT 1 FROM user_info ui WHERE ui.user_id = u.id
  );