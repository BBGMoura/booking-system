-- Dev data only: Group classes will from now on only come from timetables.
DELETE FROM booking_status
WHERE booking_id IN (
    SELECT b.id FROM booking b
    JOIN dance_class dc ON dc.id = b.dance_class_id
    WHERE dc.class_type = 'GROUP');

DELETE FROM booking
WHERE dance_class_id IN (SELECT id FROM dance_class WHERE class_type = 'GROUP');

ALTER TABLE booking ADD COLUMN booking_type VARCHAR(50);

UPDATE booking b
SET booking_type = dc.class_type
FROM dance_class dc
WHERE dc.id = b.dance_class_id;

ALTER TABLE booking ALTER COLUMN booking_type SET NOT NULL;

ALTER TABLE booking ADD CONSTRAINT chk_booking_type
    CHECK (booking_type IN ('PRIVATE', 'PRACTICE', 'GROUP', 'UNAVAILABLE', 'OTHER'));

ALTER TABLE booking RENAME COLUMN user_id TO booked_for_id;
ALTER TABLE booking RENAME CONSTRAINT fk_booking_user TO fk_booking_booked_for;
ALTER INDEX idx_booking_user_id RENAME TO idx_booking_booked_for_id;
ALTER TABLE booking ALTER COLUMN booked_for_id DROP NOT NULL;

ALTER TABLE booking ADD COLUMN created_by_id BIGINT;
UPDATE booking SET created_by_id = booked_for_id WHERE booked_for_id IS NOT NULL;
ALTER TABLE booking ALTER COLUMN created_by_id SET NOT NULL;
ALTER TABLE booking ADD CONSTRAINT fk_booking_created_by
    FOREIGN KEY (created_by_id) REFERENCES users(id);
CREATE INDEX idx_booking_created_by_id ON booking (created_by_id);

ALTER TABLE booking ALTER COLUMN dance_class_id DROP NOT NULL;

-- Closed bookings become the studio's.
UPDATE booking
SET booked_for_id = NULL,
    dance_class_id = NULL,
    total_price = 0,
    shareable = FALSE
WHERE booking_type = 'UNAVAILABLE';

DELETE FROM dance_class WHERE class_type IN ('GROUP', 'UNAVAILABLE');

ALTER TABLE dance_class DROP CONSTRAINT chk_dance_class_type;
ALTER TABLE dance_class ADD CONSTRAINT chk_dance_class_type
    CHECK (class_type IN ('PRIVATE', 'PRACTICE', 'OTHER'));

ALTER TABLE booking ADD CONSTRAINT chk_booking_studio_has_no_person
    CHECK ((booking_type IN ('GROUP', 'UNAVAILABLE')) = (booked_for_id IS NULL));

ALTER TABLE booking ADD CONSTRAINT chk_booking_studio_use_is_free
    CHECK (booking_type NOT IN ('GROUP', 'UNAVAILABLE')
        OR (total_price = 0 AND NOT shareable AND dance_class_id IS NULL));

ALTER TABLE booking ADD CONSTRAINT chk_booking_room_hire_has_floor_fee
    CHECK (booking_type IN ('GROUP', 'UNAVAILABLE') OR dance_class_id IS NOT NULL);

DROP TABLE payment;
DROP TABLE account;
