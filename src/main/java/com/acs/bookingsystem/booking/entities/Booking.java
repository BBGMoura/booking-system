
package com.acs.bookingsystem.booking.entities;

import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.user.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ToString
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    @Column
    private boolean active;
    @Column
    private boolean shareable;
    @Column(nullable = false)
    private LocalDateTime bookedFrom;
    @Column(nullable = false)
    private LocalDateTime bookedTo;
    //TODO: move this into a different class?
    @Column(nullable = false)
    private BigDecimal totalPrice;

    public void deactivate() {
        this.active = false;
    }
}
