package com.acs.bookingsystem.booking.entities;

import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.user.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    @ManyToOne
    @JoinColumn(referencedColumnName="id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    private Room room;
    @ManyToOne
    @JoinColumn(referencedColumnName="id", nullable = false)
    private DanceClass danceClass;
    @Column(nullable = false)
    private LocalDateTime bookedFrom;
    @Column(nullable = false)
    private LocalDateTime bookedTo;
    @Column(nullable = false)
    private BigDecimal totalPrice;

    public Booking(User user, Room room, DanceClass danceClass, LocalDateTime bookedFrom, LocalDateTime bookedTo, BigDecimal totalPrice) {
        this.user = user;
        this.room = room;
        this.danceClass = danceClass;
        this.bookedFrom = bookedFrom;
        this.bookedTo = bookedTo;
        this.totalPrice = totalPrice;
    }
}
