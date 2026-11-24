-- Group class and Closed are the studio's own use of its rooms, so they're never charged and have
-- no floor fee. A booking still has to point to a dance_class row, so each keeps one active,
-- admin-only row at 0 that the floor fee endpoints hide and refuse to change.
--
-- Existing rows are deactivated rather than updated, so bookings already made keep pointing to the
-- rate they were booked at. Their stored total_price never changes either way.
UPDATE dance_class
SET active = FALSE
WHERE class_type IN ('GROUP', 'UNAVAILABLE');

INSERT INTO dance_class (class_type, active, price_per_hour, role)
VALUES ('GROUP', TRUE, 0, 'ROLE_ADMIN'),
       ('UNAVAILABLE', TRUE, 0, 'ROLE_ADMIN');
