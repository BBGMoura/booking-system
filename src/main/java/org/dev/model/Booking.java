package org.dev.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    //TODO replace id with room and use @JoinColumn annotation
    @Column(name = "room_id")
    private int roomId;

    //TODO replace id with customer and use @JoinColumn annotation
    @Column(name = "customer_id")
    private int customerId;

    @Column(name = "created_on")
    private LocalDate createdOn;

    @Column(name = "booked_from")
    private LocalDate bookedFrom;

    @Column(name = "booked_to")
    private LocalDate bookedTo;

    @Column(name = "duration_in_mins")
    private int durationInMins;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus newStatus;

    @Column(name = "total_rice")
    private BigDecimal totalPrice;

    public Booking() {
    }

    public Booking(int id, int roomId, int customerId, LocalDate createdOn, LocalDate bookedFrom, LocalDate bookedTo, int durationInMins, BookingStatus newStatus, BigDecimal totalPrice) {
        this.id = id;
        this.roomId = roomId;
        this.createdOn = createdOn;
        this.customerId = customerId;
        this.bookedFrom = bookedFrom;
        this.bookedTo = bookedTo;
        this.durationInMins = durationInMins;
        this.newStatus = newStatus;
        this.totalPrice = totalPrice;
    }

    public int getId() {
        return id;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public LocalDate getBookedFrom() {
        return bookedFrom;
    }

    public LocalDate getBookedTo() {
        return bookedTo;
    }

    public int getDurationInMins() {
        return durationInMins;
    }

    public BookingStatus getBookingStatus() {
        return newStatus;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}
