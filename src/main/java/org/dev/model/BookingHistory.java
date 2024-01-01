package org.dev.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

public class BookingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "booking_status")
    private int bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "created_on")
    private BookingStatus createdOn;

    public BookingHistory() {
    }

    public BookingHistory(int id, int bookingStatus, BookingStatus createdOn) {
        this.id = id;
        this.bookingStatus = bookingStatus;
        this.createdOn = createdOn;
    }

    public int getId() {
        return id;
    }

    public int getBookingStatus() {
        return bookingStatus;
    }

    public BookingStatus getCreatedOn() {
        return createdOn;
    }
}
