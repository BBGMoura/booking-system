package com.acs.bookingsystem.booking.entity;

import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
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

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ClassType bookingType;

  // Null means the booking is for the studio.
  @ManyToOne
  @JoinColumn(referencedColumnName = "id")
  private User bookedFor;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", nullable = false)
  private User createdBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Room room;

  // The floor fee that priced the booking; null for studio use, which costs nothing.
  @ManyToOne
  @JoinColumn(referencedColumnName = "id")
  private DanceClass danceClass;

  private boolean shareable;

  @Column(nullable = false)
  private OffsetDateTime bookedFrom;

  @Column(nullable = false)
  private OffsetDateTime bookedTo;

  @Column(nullable = false)
  private BigDecimal totalPrice;

  public boolean isForStudio() {
    return bookedFor == null;
  }

  @PrePersist
  void prePersist() {
    if (uid == null) {
      uid = UUID.randomUUID();
    }
  }
}
