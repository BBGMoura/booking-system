package org.dev.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "booking_history")
public class BookingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    //TODO add booking and use @JoinColumn annotation

    @Column(name = "created_on")
    private LocalDate createdOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_booking_status")
    private BookingStatus newBookingStatus;

    private String description;

    public BookingHistory() {
    }

    public BookingHistory(LocalDate createdOn, BookingStatus newBookingStatus, String description) {
        this.createdOn = createdOn;
        this.newBookingStatus = newBookingStatus;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public BookingStatus getNewBookingStatus() {
        return newBookingStatus;
    }

    public String getDescription() {
        return description;
    }
}
