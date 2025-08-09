CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    locked BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT FALSE
);

CREATE TABLE user_info (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(11) NOT NULL,
    user_id INTEGER NOT NULL UNIQUE,
    CONSTRAINT fk_user_info_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE dance_class (
    id SERIAL PRIMARY KEY,
    class_type VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    price_per_hour DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE account (
    id SERIAL PRIMARY KEY,
    outstanding_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00
);

CREATE TABLE booking (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    room VARCHAR(50) NOT NULL,
    dance_class_id INTEGER NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    shareable BOOLEAN DEFAULT FALSE,
    booked_from TIMESTAMP NOT NULL,
    booked_to TIMESTAMP NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_booking_dance_class FOREIGN KEY (dance_class_id) REFERENCES dance_class(id)
);

CREATE TABLE booking_history (
    id SERIAL PRIMARY KEY,
    booking_id INTEGER,
    booking_status VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_booking_history_booking FOREIGN KEY (booking_id) REFERENCES booking(id),
    CONSTRAINT fk_booking_history_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE payment (
    id SERIAL PRIMARY KEY,
    booking_id INTEGER NOT NULL UNIQUE,
    payment_status VARCHAR(50) NOT NULL,
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    account_id INTEGER,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES booking(id),
    CONSTRAINT fk_payment_account FOREIGN KEY (account_id) REFERENCES account(id)
);


ALTER TABLE users ADD CONSTRAINT chk_users_role
    CHECK (role IN ('ROLE_ADMIN', 'ROLE_USER'));

ALTER TABLE dance_class ADD CONSTRAINT chk_dance_class_type
    CHECK (class_type IN ('PRIV', 'PRA', 'GRP', 'UN', 'OTH'));

ALTER TABLE dance_class ADD CONSTRAINT chk_dance_class_role
    CHECK (role IN ('ROLE_ADMIN', 'ROLE_USER'));

ALTER TABLE account ADD CONSTRAINT chk_account_balance
    CHECK (outstanding_balance >= 0);

ALTER TABLE booking ADD CONSTRAINT chk_booking_dates
    CHECK (booked_to > booked_from);

ALTER TABLE booking ADD CONSTRAINT chk_booking_price
    CHECK (total_price >= 0);

ALTER TABLE booking ADD CONSTRAINT chk_booking_room
    CHECK (room IN ('ASTA', 'BUSS', 'ALEX', 'FOS'));

ALTER TABLE booking_history ADD CONSTRAINT chk_booking_status
    CHECK (booking_status IN ('B', 'C'));

ALTER TABLE payment ADD CONSTRAINT chk_payment_status
    CHECK (payment_status IN ('O', 'P', 'V'));



