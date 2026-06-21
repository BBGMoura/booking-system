package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.booking.entity.BookingStatus;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingStatusRepository extends JpaRepository<BookingStatus, Long> {

  @Query(
      "SELECT bs FROM BookingStatus bs WHERE bs.booking.id = :bookingId ORDER BY bs.createdOn DESC LIMIT 1")
  Optional<BookingStatus> findLatestByBookingId(@Param("bookingId") Long bookingId);

  @Query(
      "SELECT bs.status FROM BookingStatus bs WHERE bs.booking.id = :bookingId ORDER BY bs.createdOn DESC LIMIT 1")
  Optional<BookingStatusType> findLatestStatusByBookingId(@Param("bookingId") Long bookingId);
}
