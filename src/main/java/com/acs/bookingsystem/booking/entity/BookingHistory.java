package com.acs.bookingsystem.booking.entity;

import com.acs.bookingsystem.booking.enums.BookingStatus;
import com.acs.bookingsystem.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingHistory {
    @Id
    @GeneratedValue
    private int id;
    @ManyToOne
    @JoinColumn(referencedColumnName="id")
    private Booking booking;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus;
    private String description;
    @Column(nullable = false)
    private LocalDateTime createdOn;
    @ManyToOne
    @JoinColumn(referencedColumnName="id", nullable = false)
    private User user;
}
