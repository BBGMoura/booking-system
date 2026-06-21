CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    uid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    version BIGINT DEFAULT 0 NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    locked BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT FALSE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(11)
);

CREATE TABLE dance_class (
    id BIGSERIAL PRIMARY KEY,
    class_type VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    price_per_hour DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    outstanding_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00
);

CREATE TABLE booking (
    id BIGSERIAL PRIMARY KEY,
    uid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    room VARCHAR(50) NOT NULL,
    dance_class_id BIGINT NOT NULL,
    shareable BOOLEAN DEFAULT FALSE,
    booked_from TIMESTAMP NOT NULL,
    booked_to TIMESTAMP NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_booking_dance_class FOREIGN KEY (dance_class_id) REFERENCES dance_class(id)
);

CREATE TABLE booking_status (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by_id BIGINT NOT NULL,
    CONSTRAINT fk_booking_status_booking FOREIGN KEY (booking_id) REFERENCES booking(id),
    CONSTRAINT fk_booking_status_user FOREIGN KEY (created_by_id) REFERENCES users(id)
);

CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    payment_status VARCHAR(50) NOT NULL,
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    account_id BIGINT,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES booking(id),
    CONSTRAINT fk_payment_account FOREIGN KEY (account_id) REFERENCES account(id)
);

ALTER TABLE users ADD CONSTRAINT chk_users_role
    CHECK (role IN ('ROLE_ADMIN', 'ROLE_USER'));

ALTER TABLE dance_class ADD CONSTRAINT chk_dance_class_type
    CHECK (class_type IN ('PRIVATE', 'PRACTICE', 'GROUP', 'UNAVAILABLE', 'OTHER'));

ALTER TABLE dance_class ADD CONSTRAINT chk_dance_class_role
    CHECK (role IN ('ROLE_ADMIN', 'ROLE_USER'));

ALTER TABLE account ADD CONSTRAINT chk_account_balance
    CHECK (outstanding_balance >= 0);

ALTER TABLE booking ADD CONSTRAINT chk_booking_dates
    CHECK (booked_to > booked_from);

ALTER TABLE booking ADD CONSTRAINT chk_booking_price
    CHECK (total_price >= 0);

ALTER TABLE booking ADD CONSTRAINT chk_booking_room
    CHECK (room IN ('ASTAIRE', 'BUSSELL', 'ALEX MOORE', 'FOSSE'));

ALTER TABLE booking_status ADD CONSTRAINT chk_booking_status_type
    CHECK (status IN ('BOOKED', 'CANCELLED'));

CREATE INDEX idx_booking_status_booking_id_created_on
    ON booking_status (booking_id, created_on DESC);

CREATE INDEX idx_booking_room_dates
    ON booking (room, booked_from, booked_to);

ALTER TABLE payment ADD CONSTRAINT chk_payment_status
    CHECK (payment_status IN ('OUTSTANDING', 'PAID', 'VOIDED'));
