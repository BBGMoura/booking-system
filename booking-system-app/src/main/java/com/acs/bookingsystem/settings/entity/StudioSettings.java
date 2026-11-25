package com.acs.bookingsystem.settings.entity;

import com.acs.bookingsystem.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "studio_settings")
@Getter
@Setter
@NoArgsConstructor
public class StudioSettings {

  /** The table holds exactly one row; a database CHECK enforces this id. */
  public static final int SINGLETON_ID = 1;

  @Id private Integer id = SINGLETON_ID;

  @Column(nullable = false)
  private String studioName;

  @Column(nullable = false)
  private int recurringMaxDates;

  @Column(nullable = false)
  private int recurringMaxActive;

  @Column(nullable = false)
  private int sharedRoomMax;

  @Column(nullable = false)
  private BigDecimal sharedDiscountPerHour;

  @Column(nullable = false)
  private int cancellationWindowHours;

  @Column(nullable = false)
  private int paymentDueDays;

  @Column(nullable = false)
  private int billingDay;

  @Column(nullable = false)
  private String invoicePrefix;

  private String businessName;
  private String businessAddress;
  private String bankAccountName;
  private String bankSortCode;
  private String bankAccountNumber;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by_id")
  private User updatedBy;
}
