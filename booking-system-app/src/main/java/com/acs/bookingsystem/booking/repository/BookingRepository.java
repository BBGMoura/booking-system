package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.Room;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository
    extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

  // A room's schedule spans many bookers and the detail view reads each booker's name, so the user
  // is fetched here rather than with a follow-up select per booker.
  @Query(
      "SELECT b FROM Booking b "
          + "JOIN FETCH b.user "
          + "WHERE b.room = :room "
          + "AND (:shareable IS NULL OR b.shareable = :shareable) "
          + "AND (SELECT bs.status FROM BookingStatus bs WHERE bs.booking = b ORDER BY bs.createdOn DESC LIMIT 1) = 'BOOKED' "
          + "AND (b.bookedFrom < :dateTo AND b.bookedTo > :dateFrom) "
          + "ORDER BY b.bookedFrom ASC")
  List<Booking> findActiveBookingsForRoomAndTimeRange(
      @Param("room") Room room,
      @Param("shareable") Boolean shareable,
      @Param("dateFrom") OffsetDateTime dateFrom,
      @Param("dateTo") OffsetDateTime dateTo);

  Optional<Booking> findByUid(UUID uid);

  Optional<Booking> findByUidAndUserId(UUID uid, Long userId);

  @Query(
      "SELECT b FROM Booking b "
          + "WHERE b.user.id = :userId "
          + "AND b.bookedTo > CURRENT_TIMESTAMP "
          + "AND (SELECT bs.status FROM BookingStatus bs WHERE bs.booking = b ORDER BY bs.createdOn DESC LIMIT 1) = 'BOOKED'")
  List<Booking> findActiveBookingsByUserId(@Param("userId") Long userId);
}
