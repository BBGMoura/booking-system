package com.acs.bookingsystem.booking;

import com.acs.bookingsystem.user.User;
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
    @JoinColumn(name="id")
    private Booking booking;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus;
    @Column(nullable = false)
    private LocalDateTime createdOn;
    @ManyToOne
    @JoinColumn(name="id", nullable = false)
    private User updatingUser;
}
