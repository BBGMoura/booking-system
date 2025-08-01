package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    /**
     * Retrieves active, sorted bookings for a room's timetable that overlap a given time range.
     * This method filters the bookings based on the following criteria:
     *  - The booking is active.
     *  - The booking is associated with the specified room.
     *  - The booking can be or not be shareable. </li>
     *  - The time range of the booking overlaps with the specified time range from {@code dateFrom} to {@code dateTo}
     *    in any of the following ways:
     *           - The requested time falls entirely within the bounds of a booking.
     *           - The requested time completely surrounds a booking.
     *           - The start time of the requested range falls within the booking, and ends after the booking starts.
     *           - The end time of the requested range is within the booking, and begins before the booking ends.
     *
     * @param room The room associated with the bookings to be retrieved.
     * @param shareable A boolean flag to filter by shareable status. If null, shareable status is not considered.
     * @param dateFrom The start of the time range to check bookings against (inclusive).
     * @param dateTo The end of the time range to check bookings against (inclusive).
     * @return A list of {@link Booking} instances that meet the criteria.
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE b.room = :room " +
            "AND b.active = true " +
            "AND (:shareable IS NULL OR b.shareable = :shareable) " +
            "AND (" +
            "     (:dateFrom > b.bookedFrom AND :dateTo < b.bookedTo) OR" +
            "     (b.bookedFrom >= :dateFrom AND b.bookedTo <= :dateTo) OR " +
            "     (b.bookedFrom > :dateFrom AND b.bookedFrom < :dateTo AND :dateTo < b.bookedTo) OR " +
            "     (b.bookedTo > :dateFrom AND b.bookedFrom < :dateFrom AND b.bookedTo < :dateTo)" +
            ")" +
            "ORDER BY b.bookedFrom ASC")
    List<Booking> findActiveBookingsForRoomAndTimeRange(@Param("room") Room room,
                                                        @Param("shareable") Boolean shareable,
                                                        @Param("dateFrom") LocalDateTime dateFrom,
                                                        @Param("dateTo") LocalDateTime dateTo);

    Page<Booking> findAllByUserId(int userId, Pageable pageable);

    Optional<Booking> findByIdAndUserId(int bookingId, int userId);

    @Query("UPDATE Booking b SET b.active = false WHERE b.user.id = :userId")
    int deactivateBookingsByUserId(@Param("userId") int userId);
}
