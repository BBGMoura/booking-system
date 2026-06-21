package com.acs.bookingsystem.booking.entity;

import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@ToString
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "uid", unique = true, nullable = false, updatable = false)
  private UUID uid;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Room room;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", nullable = false)
  private DanceClass danceClass;

  private boolean shareable;

  @Column(nullable = false)
  private LocalDateTime bookedFrom;

  @Column(nullable = false)
  private LocalDateTime bookedTo;

  @Column(nullable = false)
  private BigDecimal totalPrice;

  @PrePersist
  void prePersist() {
    if (uid == null) {
      uid = UUID.randomUUID();
    }
  }
}
