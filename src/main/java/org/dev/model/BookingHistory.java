package org.dev.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Table(name = "booking_history")
public class BookingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "created_on")
    private LocalDate createdOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus bookingStatus;

    private String description;

    public BookingHistory() {
    }

    public BookingHistory(int id, LocalDate createdOn, BookingStatus bookingStatus, String description) {
        this.id = id;
        this.createdOn = createdOn;
        this.bookingStatus = bookingStatus;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public String getDescription() {
        return description;
    }
}
