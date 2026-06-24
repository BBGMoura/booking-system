package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.Room;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

  @Query(
      "SELECT b FROM Booking b "
          + "WHERE b.room = :room "
          + "AND (:shareable IS NULL OR b.shareable = :shareable) "
          + "AND (SELECT bs.status FROM BookingStatus bs WHERE bs.booking = b ORDER BY bs.createdOn DESC LIMIT 1) = 'BOOKED' "
          + "AND (b.bookedFrom < :dateTo AND b.bookedTo > :dateFrom) "
          + "ORDER BY b.bookedFrom ASC")
  List<Booking> findActiveBookingsForRoomAndTimeRange(
      @Param("room") Room room,
      @Param("shareable") Boolean shareable,
      @Param("dateFrom") LocalDateTime dateFrom,
      @Param("dateTo") LocalDateTime dateTo);

  Page<Booking> findAllByUserId(Long userId, Pageable pageable);

  @Query("SELECT b FROM Booking b JOIN b.user u WHERE u.uid = :userUid")
  Page<Booking> findAllByUserUid(@Param("userUid") UUID userUid, Pageable pageable);

  Optional<Booking> findByUid(UUID uid);

  Optional<Booking> findByUidAndUserId(UUID uid, Long userId);

  @Query(
      "SELECT b FROM Booking b "
          + "WHERE b.user.id = :userId "
          + "AND (SELECT bs.status FROM BookingStatus bs WHERE bs.booking = b ORDER BY bs.createdOn DESC LIMIT 1) = 'BOOKED'")
  List<Booking> findActiveBookingsByUserId(@Param("userId") Long userId);
}
