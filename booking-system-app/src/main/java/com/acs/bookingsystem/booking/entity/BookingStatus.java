package com.acs.bookingsystem.booking.entity;

import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking_status")
public class BookingStatus {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", nullable = false)
  private Booking booking;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BookingStatusType status;

  @Column(nullable = false)
  private LocalDateTime createdOn;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", nullable = false)
  private User createdBy;

  @PrePersist
  void prePersist() {
    if (createdOn == null) {
      createdOn = LocalDateTime.now(ZoneId.systemDefault());
    }
  }
}
