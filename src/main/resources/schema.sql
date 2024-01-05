create schema booking_system;

use booking_system;

create table room (
    id int auto_increment,
    name varchar(30) NOT NULL,
    price_per_half_hour decimal(10,2),
    price_per_hour decimal(10,2),
    PRIMARY KEY(id)
);

create table customer (
    id int auto_increment,
    first_name varchar(50) NOT NULL,
    last_name varchar(50) NOT NULL,
    email varchar(250) NOT NULL UNIQUE,
    PRIMARY KEY(id)
);

create table booking (
    id int auto_increment,
    room_id int NOT NULL,
    customer_id int NOT NULL,
    created_on datetime NOT NULL,
    booked_from datetime NOT NULL,
    booked_to datetime NOT NULL,
    duration_in_minutes int NOT NULL,
    booking_status varchar(30) NOT NULL,
    total_price decimal(10,2) NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(room_id) REFERENCES room(id),
    FOREIGN KEY(customer_id) REFERENCES customer(id)
);

create table booking_history (
    id int auto_increment,
    booking_id int NOT NULL,
    new_booking_status varchar(30) NOT NULL,
    createdOn datetime NOT NULL,
    description varchar(50) NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(booking_id) REFERENCES booking(id)
);

insert into room
(name, price_per_half_hour, price_per_hour)
values
('Room 1', 1, 0.89),
('Room 2', 1.08, 0.78),
('Room 3', 1.2, 0.92),
('Room 4', 0.95, 0.72);
