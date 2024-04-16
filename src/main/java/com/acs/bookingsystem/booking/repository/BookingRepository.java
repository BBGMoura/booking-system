package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.booking.entities.Booking;
import com.acs.bookingsystem.booking.enums.Room;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends CrudRepository<Booking, Integer> {
    @Query("SELECT b FROM Booking b " +
            "WHERE b.room = :room " +
            "AND b.active = true " +
            "AND (" +
            "     (b.bookedFrom > :dateFrom AND b.bookedTo < :dateTo) OR " +
            "     (b.bookedFrom > :dateFrom AND b.bookedFrom < :dateTo) OR " +
            "     (b.bookedTo > :dateFrom AND b.bookedTo < :dateTo)" +
            ")")
    List<Booking> findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(@Param("room") Room room,
                                                                        @Param("dateFrom") LocalDateTime dateFrom,
                                                                        @Param("dateTo") LocalDateTime dateTo);

    List<Booking> findAllByUserId(int userId);
}
