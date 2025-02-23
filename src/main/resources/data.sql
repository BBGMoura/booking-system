MERGE INTO users AS target
USING (SELECT 'admin@acs.com' AS email,
              '$2a$10$bbSF.JLzs9zICQUtWnkl2ux5Zzgk/piP2tKJQR80QkrSLjFl.ThDy' AS password,
              'ADMIN' AS permission,
              FALSE AS locked,
              TRUE AS enabled) AS source
ON target.email = source.email
WHEN NOT MATCHED THEN
    INSERT (email, password, permission, locked, enabled)
    VALUES (source.email, source.password, source.permission, source.locked, source.enabled);

MERGE INTO user_info AS target
USING (SELECT 'Admin' AS first_name,
              'Default' AS last_name,
              '07234567890' AS phone_number,
              id AS user_id
       FROM users WHERE email = 'admin@example.com') AS source
ON target.user_id = source.user_id
WHEN NOT MATCHED THEN
    INSERT (first_name, last_name, phone_number, user_id)
    VALUES (source.first_name, source.last_name, source.phone_number, source.user_id);

