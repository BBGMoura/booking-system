package com.acs.bookingsystem.model.booking;

import com.acs.bookingsystem.model.user.User;
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
    @JoinColumn(name="id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name="id", nullable = false)
    private Room room;
    @ManyToOne
    @JoinColumn(name="id", nullable = false)
    private DanceClass danceClass;
    @Column(nullable = false)
    private LocalDateTime bookedFrom;
    @Column(nullable = false)
    private LocalDateTime bookedTo;
    @Column(nullable = false)
    private BigDecimal totalPrice;
}
