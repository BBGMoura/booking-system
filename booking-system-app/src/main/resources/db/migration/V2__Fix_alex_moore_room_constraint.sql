ALTER TABLE booking DROP CONSTRAINT chk_booking_room;

ALTER TABLE booking ADD CONSTRAINT chk_booking_room
    CHECK (room IN ('ASTAIRE', 'BUSSELL', 'ALEX_MOORE', 'FOSSE'));
