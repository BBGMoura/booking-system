package com.acs.bookingsystem.payment;

import com.acs.bookingsystem.booking.entity.Booking;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(referencedColumnName = "id", nullable = false)
  private Booking booking;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus paymentStatus;

  @Column(nullable = false)
  private Instant createdOn;

  @PrePersist
  void prePersist() {
    if (createdOn == null) {
      createdOn = Instant.now();
    }
  }

  @ManyToOne
  @JoinColumn(referencedColumnName = "id")
  private Account account;
}
