package com.acs.bookingsystem.booking.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.acs.bookingsystem.booking.config.ScheduleConfig;
import com.acs.bookingsystem.booking.entities.Booking;
import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.userold.entities.userOld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingManagerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ScheduleConfig scheduleConfig;

    @InjectMocks
    private BookingManager bookingManager;

    @Test
    void testValidateBookingTime_BookingLessThan15Minutes() {
        BookingRequest bookingRequest = new BookingRequest(
                1,
                Room.ASTAIRE,
                ClassType.PRIVATE,
                false,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(10)
        );

        Optional<String> result = bookingManager.validateBookingTime(bookingRequest);

        assertEquals(Optional.of("Booking must be a minimum of 15 minutes."), result);
    }

    @Test
    void testValidateBookingTime_InvalidInterval() {
        BookingRequest bookingRequest = new BookingRequest(
                1,
                Room.ASTAIRE,
                ClassType.PRIVATE,
                false,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(17)
        );

        Optional<String> result = bookingManager.validateBookingTime(bookingRequest);

        assertEquals(Optional.of("Booking interval is invalid. Must be in intervals of 5 minutes."), result);
    }

    @Test
    void testValidateBookingTime_StartsAfterEnds() {
        LocalDateTime now = LocalDateTime.of(2024, Month.JUNE,1,10,0);
        BookingRequest bookingRequest = new BookingRequest(
                1,
                Room.ASTAIRE,
                ClassType.PRIVATE,
                false,
                now.plusHours(1),
                now
        );

        Optional<String> result = bookingManager.validateBookingTime(bookingRequest);

        assertEquals(Optional.of("Booking start time is after end time."), result);
    }

    @Test
    void testValidateBookingTime_OverlapsNonShareableExistingBookings() {
        LocalDateTime now = LocalDateTime.of(2024, Month.JUNE,1,10,0);
        BookingRequest bookingRequest = new BookingRequest(
                1,
                Room.ASTAIRE,
                ClassType.PRIVATE,
                false,
                now,
                now.plusMinutes(30)
        );

        when(bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(
                Room.ASTAIRE, false, now, now.plusMinutes(30)
        )).thenReturn(Collections.singletonList(new Booking()));

        Optional<String> result = bookingManager.validateBookingTime(bookingRequest);

        assertEquals(Optional.of("Booking timeslot is unavailable."), result);
    }

    @Test
    void testValidateBookingTime_NotSameDate() {
        LocalDateTime now = LocalDateTime.of(2024, Month.JUNE,1,10,0);
        BookingRequest bookingRequest = new BookingRequest(
                1,
                Room.ASTAIRE,
                ClassType.PRIVATE,
                false,
                now,
                now.plusDays(1)
        );

        Optional<String> result = bookingManager.validateBookingTime(bookingRequest);

        assertEquals(Optional.of("Booking must start and end on the same day."), result);
    }

    @Test
    void testValidateBookingTime_NotWithinOpeningTime() {
        LocalDateTime now = LocalDateTime.of(2024, Month.JUNE,27,10,0);
        BookingRequest bookingRequest = new BookingRequest(
                1,
                Room.ASTAIRE,
                ClassType.PRIVATE,
                false,
                now.withHour(6),
                now.withHour(7)
        );

        when(scheduleConfig.getWeekdayOpening()).thenReturn(LocalTime.of(9, 0));
        when(scheduleConfig.getWeekdayClosing()).thenReturn(LocalTime.of(17, 0));

        Optional<String> result = bookingManager.validateBookingTime(bookingRequest);

        assertEquals(Optional.of("Cannot make a booking as booking time is not within opening times."), result);
    }

    @Test
    void testValidateBookingTime_ShareableBookingOverlapsThreeOrMoreShareableBookings() {
        LocalDateTime now =  LocalDateTime.of(2024, Month.JUNE,27,10,0);
        BookingRequest bookingRequest = new BookingRequest(
                1,
                Room.ASTAIRE,
                ClassType.PRIVATE,
                true,
                now,
                now.plusMinutes(30)
        );

        Booking booking1 = createBooking(new userOld(), new DanceClass(), now, now.plusMinutes(30), BigDecimal.TEN);
        Booking booking2 = createBooking(new userOld(), new DanceClass(), now, now.plusMinutes(30), BigDecimal.TEN);
        Booking booking3 = createBooking(new userOld(), new DanceClass(), now, now.plusMinutes(30), BigDecimal.TEN);


        when(bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(
                Room.ASTAIRE, true, now, now.plusMinutes(30)
        )).thenReturn(List.of(booking1, booking2,booking3));

        Optional<String> result = bookingManager.validateSharedBookingRequest(bookingRequest);

        assertEquals(Optional.of("Booking as timeslot is unavailable. Can only book 3 at a time."), result);
    }

    private Booking createBooking(userOld userOld, DanceClass danceClass, LocalDateTime from, LocalDateTime to, BigDecimal price) {
        return Booking.builder()
                      .userOld(userOld)
                      .room(Room.ASTAIRE)
                      .danceClass(danceClass)
                      .active(true)
                      .shareable(true)
                      .bookedFrom(from)
                      .bookedTo(to)
                      .totalPrice(price)
                      .build();
    }
}
