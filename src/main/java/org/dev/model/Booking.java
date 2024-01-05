package org.dev.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

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
    private BookingStatus bookingStatus;

    @Column(name = "total_rice")
    private BigDecimal totalPrice;

    public Booking() {}

    public Booking(Room room, Customer customer, LocalDate createdOn, LocalDate bookedFrom, LocalDate bookedTo, int durationInMins, BookingStatus bookingStatus, BigDecimal totalPrice) {
        this.room = room;
        this.createdOn = createdOn;
        this.customer = customer;
        this.bookedFrom = bookedFrom;
        this.bookedTo = bookedTo;
        this.durationInMins = durationInMins;
        this.bookingStatus = bookingStatus;
        this.totalPrice = totalPrice;
    }

    public int getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public Customer getCustomer() {
        return customer;
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
        return bookingStatus;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}
