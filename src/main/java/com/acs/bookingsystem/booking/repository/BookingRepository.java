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
     * Retrieves a list of active bookings for a specified room that intersect with a given time range.
     * This method filters the bookings based on the following criteria:
     * <ul>
     *     <li>The booking is active.</li>
     *     <li>The booking is associated with the specified room.</li>
     *     <li>The booking can be or not be shareable. </li>
     *     <li>The time range of the booking overlaps with the specified time range from {@code dateFrom} to {@code dateTo} in any of the following ways:
     *         <ol>
     *             <li>The requested time falls entirely within the bounds of a booking.</li>
     *             <li>The requested time completely surrounds a booking.</li>
     *             <li>The start time of the requested range falls within the booking, and ends after the booking starts.</li>
     *             <li>The end time of the requested range is within the booking, and begins before the booking ends.</li>
     *         </ol>
     *     </li>
     * </ul>
     *
     * @param room The room associated with the bookings to be retrieved.
     * @param dateFrom The start of the time range to check bookings against.
     * @param dateTo The end of the time range to check bookings against.
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
            ")")
    List<Booking> findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(@Param("room") Room room,
                                                                        @Param("shareable") Boolean shareable,
                                                                        @Param("dateFrom") LocalDateTime dateFrom,
                                                                        @Param("dateTo") LocalDateTime dateTo);

    Page<Booking> findAllByUserId(int userId, Pageable pageable);

    Optional<Booking> findByIdAndUserId(int bookingId, int userId);
}
