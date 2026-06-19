INSERT INTO users (email, password, role, locked, enabled, first_name, last_name, phone_number)
VALUES ('admin@acs.com',
        '$2a$10$bbSF.JLzs9zICQUtWnkl2ux5Zzgk/piP2tKJQR80QkrSLjFl.ThDy',
        'ROLE_ADMIN',
        FALSE,
        TRUE,
        'Admin',
        'Default',
        '07234567890')
ON CONFLICT (email) DO NOTHING;
