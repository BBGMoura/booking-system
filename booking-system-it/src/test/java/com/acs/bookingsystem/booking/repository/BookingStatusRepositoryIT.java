package com.acs.bookingsystem.booking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.acs.bookingsystem.BaseIntegrationTest;
import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.entity.BookingStatus;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.repository.DanceClassRepository;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class BookingStatusRepositoryIT extends BaseIntegrationTest {

  @Autowired private BookingRepository bookingRepository;
  @Autowired private BookingStatusRepository bookingStatusRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private DanceClassRepository danceClassRepository;

  private User user;
  private Booking booking;

  @BeforeEach
  void setup() {
    user =
        userRepository.save(
            User.builder()
                .uid(UUID.randomUUID())
                .email(UUID.randomUUID() + "@test.com")
                .role(Role.ROLE_USER)
                .locked(false)
                .enabled(true)
                .build());

    DanceClass danceClass =
        danceClassRepository.save(
            new DanceClass(null, ClassType.PRIVATE, true, BigDecimal.TEN, Role.ROLE_USER));

    booking =
        bookingRepository.save(
            Booking.builder()
                .uid(UUID.randomUUID())
                .user(user)
                .room(Room.ASTAIRE)
                .danceClass(danceClass)
                .shareable(false)
                .bookedFrom(LocalDateTime.of(2025, 6, 2, 10, 0))
                .bookedTo(LocalDateTime.of(2025, 6, 2, 11, 0))
                .totalPrice(BigDecimal.TEN)
                .build());
  }

  @Test
  void findLatestStatusByBookingId_returnsEmpty_whenNoStatusExists() {
    Optional<BookingStatusType> result =
        bookingStatusRepository.findLatestStatusByBookingId(booking.getId());

    assertThat(result).isEmpty();
  }

  @Test
  void findLatestStatusByBookingId_returnsSingleStatus() {
    saveStatus(BookingStatusType.BOOKED, LocalDateTime.of(2025, 1, 1, 10, 0));

    Optional<BookingStatusType> result =
        bookingStatusRepository.findLatestStatusByBookingId(booking.getId());

    assertThat(result).contains(BookingStatusType.BOOKED);
  }

  @Test
  void findLatestStatusByBookingId_returnsLatestWhenMultipleExist() {
    saveStatus(BookingStatusType.BOOKED, LocalDateTime.of(2025, 1, 1, 10, 0));
    saveStatus(BookingStatusType.CANCELLED, LocalDateTime.of(2025, 1, 1, 11, 0));

    Optional<BookingStatusType> result =
        bookingStatusRepository.findLatestStatusByBookingId(booking.getId());

    assertThat(result).contains(BookingStatusType.CANCELLED);
  }

  @Test
  void findLatestByBookingId_returnsFullStatusEntity() {
    saveStatus(BookingStatusType.BOOKED, LocalDateTime.of(2025, 1, 1, 10, 0));
    saveStatus(BookingStatusType.CANCELLED, LocalDateTime.of(2025, 1, 1, 11, 0));

    Optional<BookingStatus> result = bookingStatusRepository.findLatestByBookingId(booking.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getStatus()).isEqualTo(BookingStatusType.CANCELLED);
    assertThat(result.get().getCreatedBy().getId()).isEqualTo(user.getId());
  }

  private void saveStatus(BookingStatusType type, LocalDateTime createdOn) {
    bookingStatusRepository.save(
        BookingStatus.builder()
            .booking(booking)
            .status(type)
            .createdBy(user)
            .createdOn(createdOn)
            .build());
  }
}
