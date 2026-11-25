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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingMapperTest {

  private BookingMapper mapper;

  private static final UUID BOOKING_UID = UUID.randomUUID();
  private static final UUID USER_UID = UUID.randomUUID();
  private static final OffsetDateTime FROM =
      LocalDateTime.of(2025, Month.JUNE, 2, 10, 0).atOffset(ZoneOffset.UTC);
  private static final OffsetDateTime TO =
      LocalDateTime.of(2025, Month.JUNE, 2, 11, 0).atOffset(ZoneOffset.UTC);

  private Booking booking;

  @BeforeEach
  void setup() {
    mapper = new BookingMapper();

    User user =
        User.builder()
            .uid(USER_UID)
            .firstName("Ginger")
            .lastName("Rogers")
            .role(Role.ROLE_USER)
            .build();
    DanceClass danceClass =
        new DanceClass(1L, ClassType.PRACTICE, true, BigDecimal.TEN, Role.ROLE_USER);

    booking =
        Booking.builder()
            .uid(BOOKING_UID)
            .bookingType(ClassType.PRIVATE)
            .bookedFor(user)
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
    assertThat(detail.firstName()).isEqualTo("Ginger");
    assertThat(detail.lastName()).isEqualTo("Rogers");
    assertThat(detail.room()).isEqualTo(Room.ASTAIRE);
    assertThat(detail.status()).isEqualTo(BookingStatusType.BOOKED);
    assertThat(detail.shareable()).isFalse();
    assertThat(detail.classType()).isEqualTo(ClassType.PRIVATE);
    assertThat(detail.dateFrom()).isEqualTo(FROM);
    assertThat(detail.dateTo()).isEqualTo(TO);
    assertThat(detail.totalPrice()).isEqualByComparingTo(BigDecimal.TEN);
    assertThat(detail.forStudio()).isFalse();
  }

  @Test
  void mapDetail_givenStudioBooking_hasNoPersonAndIsForStudio() {
    BookingDetail detail = mapper.mapDetail(studioBooking(), BookingStatusType.BOOKED);

    assertThat(detail.userUid()).isNull();
    assertThat(detail.firstName()).isNull();
    assertThat(detail.lastName()).isNull();
    assertThat(detail.classType()).isEqualTo(ClassType.UNAVAILABLE);
    assertThat(detail.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(detail.forStudio()).isTrue();
  }

  @Test
  void mapSummary_givenStudioBooking_isForStudioWithBookingType() {
    BookingSummary summary = mapper.mapSummary(studioBooking(), BookingStatusType.BOOKED);

    assertThat(summary.classType()).isEqualTo(ClassType.UNAVAILABLE);
    assertThat(summary.forStudio()).isTrue();
  }

  private Booking studioBooking() {
    return Booking.builder()
        .uid(BOOKING_UID)
        .bookingType(ClassType.UNAVAILABLE)
        .createdBy(User.builder().uid(UUID.randomUUID()).role(Role.ROLE_ADMIN).build())
        .room(Room.ASTAIRE)
        .bookedFrom(FROM)
        .bookedTo(TO)
        .totalPrice(BigDecimal.ZERO)
        .build();
  }

  @Test
  void mapDetail_givenUserWithoutName_mapsNullName() {
    Booking unnamedBooking =
        Booking.builder()
            .uid(BOOKING_UID)
            .bookedFor(User.builder().uid(USER_UID).role(Role.ROLE_ADMIN).build())
            .room(Room.ASTAIRE)
            .danceClass(booking.getDanceClass())
            .bookedFrom(FROM)
            .bookedTo(TO)
            .totalPrice(BigDecimal.TEN)
            .build();

    BookingDetail detail = mapper.mapDetail(unnamedBooking, BookingStatusType.BOOKED);

    assertThat(detail.firstName()).isNull();
    assertThat(detail.lastName()).isNull();
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
    assertThat(summary.forStudio()).isFalse();
  }
}
