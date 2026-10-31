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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class BookingRepositoryIT extends BaseIntegrationTest {

  @Autowired private BookingRepository bookingRepository;
  @Autowired private BookingStatusRepository bookingStatusRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private DanceClassRepository danceClassRepository;

  private static final OffsetDateTime FROM =
      LocalDateTime.of(2025, Month.JUNE, 2, 10, 0).atOffset(ZoneOffset.UTC);
  private static final OffsetDateTime TO =
      LocalDateTime.of(2025, Month.JUNE, 2, 11, 0).atOffset(ZoneOffset.UTC);

  private User user;
  private DanceClass danceClass;

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

    danceClass =
        danceClassRepository.save(
            new DanceClass(null, ClassType.PRIVATE, true, BigDecimal.TEN, Role.ROLE_USER));
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_returnsBookedBookingInRange() {
    Booking booking =
        saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.BOOKED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).extracting(Booking::getUid).contains(booking.getUid());
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_detectsOverlap_whenExistingContainsNew() {
    Booking booking =
        saveBookingWithStatus(
            Room.ASTAIRE, false, FROM.minusHours(1), TO.plusHours(1), BookingStatusType.BOOKED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).extracting(Booking::getUid).contains(booking.getUid());
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_detectsOverlap_whenNewContainsExisting() {
    Booking booking =
        saveBookingWithStatus(
            Room.ASTAIRE,
            false,
            FROM.plusMinutes(30),
            TO.minusMinutes(15),
            BookingStatusType.BOOKED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).extracting(Booking::getUid).contains(booking.getUid());
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_detectsOverlap_whenExistingStartsDuringNew() {
    Booking booking =
        saveBookingWithStatus(
            Room.ASTAIRE, false, FROM.plusMinutes(30), TO.plusHours(1), BookingStatusType.BOOKED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).extracting(Booking::getUid).contains(booking.getUid());
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_detectsOverlap_whenExistingEndsDuringNew() {
    Booking booking =
        saveBookingWithStatus(
            Room.ASTAIRE,
            false,
            FROM.minusHours(1),
            FROM.plusMinutes(30),
            BookingStatusType.BOOKED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).extracting(Booking::getUid).contains(booking.getUid());
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_detectsOverlap_whenExistingHasSameStart() {
    Booking booking =
        saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO.plusHours(1), BookingStatusType.BOOKED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).extracting(Booking::getUid).contains(booking.getUid());
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_detectsOverlap_whenExistingHasSameEnd() {
    Booking booking =
        saveBookingWithStatus(
            Room.ASTAIRE, false, FROM.minusHours(1), TO, BookingStatusType.BOOKED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).extracting(Booking::getUid).contains(booking.getUid());
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_excludesAdjacentBookingEndingAtRangeStart() {
    saveBookingWithStatus(Room.ASTAIRE, false, FROM.minusHours(2), FROM, BookingStatusType.BOOKED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).isEmpty();
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_excludesAdjacentBookingStartingAtRangeEnd() {
    saveBookingWithStatus(Room.ASTAIRE, false, TO, TO.plusHours(1), BookingStatusType.BOOKED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).isEmpty();
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_excludesCancelledBookings() {
    saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.CANCELLED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).isEmpty();
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_excludesBookingsInDifferentRoom() {
    saveBookingWithStatus(Room.BUSSELL, false, FROM, TO, BookingStatusType.BOOKED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).isEmpty();
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_excludesNonOverlappingBookings() {
    OffsetDateTime before = LocalDateTime.of(2025, Month.JUNE, 2, 8, 0).atOffset(ZoneOffset.UTC);
    OffsetDateTime beforeEnd = LocalDateTime.of(2025, Month.JUNE, 2, 9, 0).atOffset(ZoneOffset.UTC);
    saveBookingWithStatus(Room.ASTAIRE, false, before, beforeEnd, BookingStatusType.BOOKED);

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).isEmpty();
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_filtersByShareable() {
    saveBookingWithStatus(Room.ASTAIRE, true, FROM, TO, BookingStatusType.BOOKED);
    saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.BOOKED);

    List<Booking> shareableOnly =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, true, FROM, TO);

    assertThat(shareableOnly).hasSize(1).allMatch(Booking::isShareable);
  }

  @Test
  void findActiveBookingsForRoomAndTimeRange_returnsLatestStatusPerBooking() {
    Booking booking = saveBooking(Room.ASTAIRE, false, FROM, TO);
    saveStatus(booking, BookingStatusType.BOOKED, Instant.parse("2025-01-01T10:00:00Z"));
    saveStatus(booking, BookingStatusType.CANCELLED, Instant.parse("2025-01-01T11:00:00Z"));

    List<Booking> result =
        bookingRepository.findActiveBookingsForRoomAndTimeRange(Room.ASTAIRE, null, FROM, TO);

    assertThat(result).isEmpty();
  }

  // --- findActiveBookingsByUserId ---

  @Test
  void findActiveBookingsByUserId_returnsUpcomingBookedBooking() {
    OffsetDateTime future = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
    Booking booking =
        saveBookingWithStatus(
            Room.ASTAIRE, false, future, future.plusHours(1), BookingStatusType.BOOKED);

    List<Booking> result = bookingRepository.findActiveBookingsByUserId(user.getId());

    assertThat(result).extracting(Booking::getUid).containsExactly(booking.getUid());
  }

  @Test
  void findActiveBookingsByUserId_excludesPastBookedBooking() {
    OffsetDateTime future = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
    Booking upcoming =
        saveBookingWithStatus(
            Room.ASTAIRE, false, future, future.plusHours(1), BookingStatusType.BOOKED);
    saveBookingWithStatus(Room.BUSSELL, false, FROM, TO, BookingStatusType.BOOKED);

    List<Booking> result = bookingRepository.findActiveBookingsByUserId(user.getId());

    assertThat(result).extracting(Booking::getUid).containsExactly(upcoming.getUid());
  }

  @Test
  void findActiveBookingsByUserId_excludesCancelledUpcomingBooking() {
    OffsetDateTime future = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
    Booking upcoming =
        saveBookingWithStatus(
            Room.ASTAIRE, false, future, future.plusHours(1), BookingStatusType.BOOKED);
    saveBookingWithStatus(
        Room.BUSSELL,
        false,
        future.plusDays(1),
        future.plusDays(1).plusHours(1),
        BookingStatusType.CANCELLED);

    List<Booking> result = bookingRepository.findActiveBookingsByUserId(user.getId());

    assertThat(result).extracting(Booking::getUid).containsExactly(upcoming.getUid());
  }

  // --- BookingSpecifications, via bookingRepository.findAll(spec, pageable) ---

  @Test
  void specifications_forUserOnly_returnsAllBookingsForUser() {
    Booking booking =
        saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.BOOKED);

    Page<Booking> result =
        bookingRepository.findAll(
            Specification.where(BookingSpecifications.forUser(user.getId())), Pageable.unpaged());

    assertThat(result.getContent()).extracting(Booking::getUid).containsExactly(booking.getUid());
  }

  @Test
  void specifications_forUser_excludesOtherUsersBookings() {
    saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.BOOKED);
    User otherUser =
        userRepository.save(
            User.builder()
                .uid(UUID.randomUUID())
                .email(UUID.randomUUID() + "@test.com")
                .role(Role.ROLE_USER)
                .locked(false)
                .enabled(true)
                .build());
    bookingRepository.save(
        Booking.builder()
            .uid(UUID.randomUUID())
            .user(otherUser)
            .room(Room.BUSSELL)
            .danceClass(danceClass)
            .shareable(false)
            .bookedFrom(FROM)
            .bookedTo(TO)
            .totalPrice(BigDecimal.TEN)
            .build());

    Page<Booking> result =
        bookingRepository.findAll(
            Specification.where(BookingSpecifications.forUser(user.getId())), Pageable.unpaged());

    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  void specifications_inRange_dateFromOnly_includesBookingAtExactBoundary() {
    Booking atBoundary =
        saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.BOOKED);

    Page<Booking> result = inRangeResult(FROM, null);

    assertThat(result.getContent())
        .extracting(Booking::getUid)
        .containsExactly(atBoundary.getUid());
  }

  @Test
  void specifications_inRange_dateFromOnly_excludesBookingStartingBeforeIt() {
    saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.BOOKED);

    Page<Booking> result = inRangeResult(FROM.plusMinutes(1), null);

    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void specifications_inRange_dateToOnly_includesBookingStartingBeforeIt() {
    Booking before = saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.BOOKED);

    Page<Booking> result = inRangeResult(null, FROM.plusMinutes(1));

    assertThat(result.getContent()).extracting(Booking::getUid).containsExactly(before.getUid());
  }

  @Test
  void specifications_inRange_dateToOnly_excludesBookingStartingAtExactBoundary() {
    saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.BOOKED);

    Page<Booking> result = inRangeResult(null, FROM);

    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void specifications_inRange_bothBounds_includesBookingStartingWithinRange() {
    Booking within = saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.BOOKED);

    Page<Booking> result = inRangeResult(FROM.minusDays(1), FROM.plusDays(1));

    assertThat(result.getContent()).extracting(Booking::getUid).containsExactly(within.getUid());
  }

  @Test
  void specifications_inRange_bothBounds_excludesBookingStartingOutsideRange() {
    saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.BOOKED);

    Page<Booking> result = inRangeResult(FROM.plusDays(1), FROM.plusDays(2));

    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void specifications_inRange_includesCancelledBookings() {
    Booking booking =
        saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.CANCELLED);

    Page<Booking> result = inRangeResult(FROM, null);

    assertThat(result.getContent()).extracting(Booking::getUid).containsExactly(booking.getUid());
  }

  @Test
  void specifications_combinesDateFromAndStatus() {
    OffsetDateTime future = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
    Booking match =
        saveBookingWithStatus(
            Room.ASTAIRE, false, future, future.plusHours(1), BookingStatusType.BOOKED);
    saveBookingWithStatus(
        Room.BUSSELL, false, future, future.plusHours(1), BookingStatusType.CANCELLED);
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    Page<Booking> result =
        bookingRepository.findAll(
            Specification.where(BookingSpecifications.forUser(user.getId()))
                .and(BookingSpecifications.inRange(now, null))
                .and(BookingSpecifications.hasLatestStatus(BookingStatusType.BOOKED)),
            Pageable.unpaged());

    assertThat(result.getContent()).extracting(Booking::getUid).containsExactly(match.getUid());
  }

  private Page<Booking> inRangeResult(OffsetDateTime dateFrom, OffsetDateTime dateTo) {
    return bookingRepository.findAll(
        Specification.where(BookingSpecifications.forUser(user.getId()))
            .and(BookingSpecifications.inRange(dateFrom, dateTo)),
        Pageable.unpaged());
  }

  @Test
  void specifications_hasLatestStatus_filtersToGivenStatus() {
    Booking booked = saveBookingWithStatus(Room.ASTAIRE, false, FROM, TO, BookingStatusType.BOOKED);
    saveBookingWithStatus(Room.BUSSELL, false, FROM, TO, BookingStatusType.CANCELLED);

    Page<Booking> result =
        bookingRepository.findAll(
            Specification.where(BookingSpecifications.forUser(user.getId()))
                .and(BookingSpecifications.hasLatestStatus(BookingStatusType.BOOKED)),
            Pageable.unpaged());

    assertThat(result.getContent()).extracting(Booking::getUid).containsExactly(booked.getUid());
  }

  @Test
  void specifications_hasLatestStatus_usesLatestStatusNotFirst() {
    Booking booking = saveBooking(Room.ASTAIRE, false, FROM, TO);
    saveStatus(booking, BookingStatusType.BOOKED, Instant.parse("2025-01-01T10:00:00Z"));
    saveStatus(booking, BookingStatusType.CANCELLED, Instant.parse("2025-01-01T11:00:00Z"));

    Page<Booking> bookedResult =
        bookingRepository.findAll(
            Specification.where(BookingSpecifications.forUser(user.getId()))
                .and(BookingSpecifications.hasLatestStatus(BookingStatusType.BOOKED)),
            Pageable.unpaged());
    Page<Booking> cancelledResult =
        bookingRepository.findAll(
            Specification.where(BookingSpecifications.forUser(user.getId()))
                .and(BookingSpecifications.hasLatestStatus(BookingStatusType.CANCELLED)),
            Pageable.unpaged());

    assertThat(bookedResult.getContent()).isEmpty();
    assertThat(cancelledResult.getContent())
        .extracting(Booking::getUid)
        .containsExactly(booking.getUid());
  }

  private Booking saveBookingWithStatus(
      Room room,
      boolean shareable,
      OffsetDateTime from,
      OffsetDateTime to,
      BookingStatusType statusType) {
    Booking booking = saveBooking(room, shareable, from, to);
    saveStatus(booking, statusType, Instant.now());
    return booking;
  }

  private Booking saveBooking(
      Room room, boolean shareable, OffsetDateTime from, OffsetDateTime to) {
    return bookingRepository.save(
        Booking.builder()
            .uid(UUID.randomUUID())
            .user(user)
            .room(room)
            .danceClass(danceClass)
            .shareable(shareable)
            .bookedFrom(from)
            .bookedTo(to)
            .totalPrice(BigDecimal.TEN)
            .build());
  }

  private void saveStatus(Booking booking, BookingStatusType type, Instant createdOn) {
    bookingStatusRepository.save(
        BookingStatus.builder()
            .booking(booking)
            .status(type)
            .createdBy(user)
            .createdOn(createdOn)
            .build());
  }
}
