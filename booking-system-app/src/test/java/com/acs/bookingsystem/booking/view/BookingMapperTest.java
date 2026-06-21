package com.acs.bookingsystem.booking.view;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.view.dto.BookingDetail;
import com.acs.bookingsystem.booking.view.dto.BookingSummary;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingMapperTest {

  private BookingMapper mapper;

  private static final UUID BOOKING_UID = UUID.randomUUID();
  private static final UUID USER_UID = UUID.randomUUID();
  private static final LocalDateTime FROM = LocalDateTime.of(2025, Month.JUNE, 2, 10, 0);
  private static final LocalDateTime TO = LocalDateTime.of(2025, Month.JUNE, 2, 11, 0);

  private Booking booking;

  @BeforeEach
  void setup() {
    mapper = new BookingMapper();

    User user = User.builder().uid(USER_UID).role(Role.ROLE_USER).build();
    DanceClass danceClass =
        new DanceClass(1L, ClassType.PRIVATE, true, BigDecimal.TEN, Role.ROLE_USER);

    booking =
        Booking.builder()
            .uid(BOOKING_UID)
            .user(user)
            .room(Room.ASTAIRE)
            .danceClass(danceClass)
            .shareable(false)
            .bookedFrom(FROM)
            .bookedTo(TO)
            .totalPrice(BigDecimal.TEN)
            .build();
  }

  @Test
  void mapDetail_mapsAllFieldsCorrectly() {
    BookingDetail detail = mapper.mapDetail(booking, BookingStatusType.BOOKED);

    assertThat(detail.uid()).isEqualTo(BOOKING_UID);
    assertThat(detail.userUid()).isEqualTo(USER_UID);
    assertThat(detail.room()).isEqualTo(Room.ASTAIRE);
    assertThat(detail.status()).isEqualTo(BookingStatusType.BOOKED);
    assertThat(detail.shareable()).isFalse();
    assertThat(detail.classType()).isEqualTo(ClassType.PRIVATE);
    assertThat(detail.dateFrom()).isEqualTo(FROM);
    assertThat(detail.dateTo()).isEqualTo(TO);
    assertThat(detail.totalPrice()).isEqualByComparingTo(BigDecimal.TEN);
  }

  @Test
  void mapDetail_reflectsCancelledStatus() {
    BookingDetail detail = mapper.mapDetail(booking, BookingStatusType.CANCELLED);

    assertThat(detail.status()).isEqualTo(BookingStatusType.CANCELLED);
  }

  @Test
  void mapSummary_mapsAllFieldsCorrectly() {
    BookingSummary summary = mapper.mapSummary(booking, BookingStatusType.BOOKED);

    assertThat(summary.uid()).isEqualTo(BOOKING_UID);
    assertThat(summary.room()).isEqualTo(Room.ASTAIRE);
    assertThat(summary.classType()).isEqualTo(ClassType.PRIVATE);
    assertThat(summary.status()).isEqualTo(BookingStatusType.BOOKED);
    assertThat(summary.shareable()).isFalse();
    assertThat(summary.bookedFrom()).isEqualTo(FROM);
    assertThat(summary.bookedTo()).isEqualTo(TO);
  }
}
