package com.acs.bookingsystem.booking.repository;

import com.acs.bookingsystem.booking.entities.Booking;
import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.user.entities.User;
import com.acs.bookingsystem.user.enums.Permission;
import com.acs.bookingsystem.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DanceClassRepository danceClassRepository;

    @Test
    void createABooking() {
        //given
        final Booking booking = createTestBooking();

        //when
        bookingRepository.save(booking);

        final int bookingId = booking.getId();
        final Booking savedBooking = bookingRepository.findById(bookingId).orElseThrow();

        final List<Booking> savedBookings = new ArrayList<>();
        bookingRepository.findAll().forEach((savedBookings::add));

        //then
        assertNotEquals(0, savedBooking.getId());
        assertEquals(1, bookingRepository.count());
        assertEquals(1, savedBookings.size());
    }

    @Test
    void findAllBookingsForRoomFromAndTo() {
        //given
        final Booking booking1 = createTestBooking();
        final Booking booking2 = createAnotherTestBooking();
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);

        //when
        List<Booking> filteredBookings = bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(Room.ASTAIRE,
                                                                                                                 false,
                                                                                                                 LocalDateTime.of(2024, Month.MARCH, 30, 12, 0),
                                                                                                                 LocalDateTime.of(2024, Month.MARCH, 30, 13, 15));

        //then
        assertEquals(2, filteredBookings.size());
        assertEquals(filteredBookings.getFirst().getBookedFrom(), LocalDateTime.of(2024, Month.MARCH, 30, 12, 0));
        assertEquals(filteredBookings.getLast().getBookedFrom(), LocalDateTime.of(2024, Month.MARCH, 30, 12, 45));


        //time overlaps the time range
        List<Booking> filteredBookings2 = bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(Room.ASTAIRE,
                                                                                                                  false,
                                                                                                                  LocalDateTime.of(2024, Month.MARCH, 30, 12, 15),
                                                                                                                  LocalDateTime.of(2024, Month.MARCH, 30, 13, 0));

        //then
        assertEquals(2, filteredBookings2.size());
        assertEquals(filteredBookings2.getFirst().getBookedFrom(), LocalDateTime.of(2024, Month.MARCH, 30, 12, 0));
        assertEquals(filteredBookings2.getLast().getBookedFrom(), LocalDateTime.of(2024, Month.MARCH, 30, 12, 45));

        // time shouldn't return any
        List<Booking> filteredBookings3 = bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(Room.ASTAIRE,
                                                                                                                  false,
                                                                                                                  LocalDateTime.of(2024, Month.MARCH, 30, 9, 0),
                                                                                                                  LocalDateTime.of(2024, Month.MARCH, 30, 9, 30));

        assertEquals(0, filteredBookings3.size());

        List<Booking> filteredBookings4 = bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(Room.ASTAIRE,
                                                                                                                  false,
                                                                                                                  LocalDateTime.of(2024, Month.MARCH, 30, 11, 0),
                                                                                                                  LocalDateTime.of(2024, Month.MARCH, 30, 12, 0));

        assertEquals(0, filteredBookings4.size());

        List<Booking> filteredBookings5 = bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(Room.ASTAIRE,
                                                                                                                  false,
                                                                                                                  LocalDateTime.of(2024, Month.MARCH, 30, 12, 20),
                                                                                                                  LocalDateTime.of(2024, Month.MARCH, 30, 12, 50));

        assertEquals(2, filteredBookings5.size());

        List<Booking> filteredBookings6 = bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(Room.ASTAIRE,
                                                                                                                  false,
                                                                                                                  LocalDateTime.of(2024, Month.MARCH, 30, 12, 20),
                                                                                                                  LocalDateTime.of(2024, Month.MARCH, 30, 12, 35));

        assertEquals(1, filteredBookings6.size());

        //booking present already within time range
        List<Booking> filteredBookings7 = bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(Room.ASTAIRE,
                                                                                                                  false,
                                                                                                                  LocalDateTime.of(2024,Month.MARCH, 30, 12,0),
                                                                                                                  LocalDateTime.of(2024, Month.MARCH,30, 12, 30));
        assertEquals(1, filteredBookings7.size());

        //booking present already within time range
        List<Booking> filteredBookings8 = bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(Room.ASTAIRE,
                                                                                                                  false,
                                                                                                                  LocalDateTime.of(2024,Month.MARCH, 30, 12,10),
                                                                                                                  LocalDateTime.of(2024, Month.MARCH,30, 12, 20));
        assertEquals(1, filteredBookings8.size());

        //booking present already within time range
        List<Booking> filteredBookings9 = bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(Room.ASTAIRE,
                                                                                                                  false,
                                                                                                                  LocalDateTime.of(2024,Month.MARCH, 30, 13,15),
                                                                                                                  LocalDateTime.of(2024, Month.MARCH,30, 13, 50));
        assertEquals(0, filteredBookings9.size());
    }

    @Test
    void deleteABooking() {
        //given
        final Booking booking = createTestBooking();
        bookingRepository.save(booking);

        //when
        final int bookingId = booking.getId();
        bookingRepository.deleteById(bookingId);
        final Optional<Booking> deletedBooking = bookingRepository.findById(bookingId);

        //then
        assertTrue(deletedBooking.isEmpty());
    }

    private Booking createTestBooking() {
        User user = new User("May",
                             "Jones",
                             "mayjones@gmail.com",
                             "01234617281",
                             true,
                             Permission.USER);
        userRepository.save(user);

        DanceClass danceClass = new DanceClass(ClassType.GROUP,
                                               true,
                                               BigDecimal.ONE,
                                               BigDecimal.ONE,
                                               BigDecimal.ONE);
        danceClassRepository.save(danceClass);

        return Booking.builder()
                      .user(user)
                      .room(Room.ASTAIRE)
                      .danceClass(danceClass)
                      .active(true)
                      .shareable(false)
                      .bookedFrom(LocalDateTime.of(2024, Month.MARCH, 30, 12, 0))
                      .bookedTo(LocalDateTime.of(2024, Month.MARCH, 30, 12, 30))
                      .totalPrice(BigDecimal.TEN)
                      .build();
    }

    private Booking createAnotherTestBooking() {
        User user = new User("May",
                             "Jones",
                             "mayjones2@gmail.com",
                             "01234617281",
                             true,
                             Permission.USER);
        userRepository.save(user);

        DanceClass danceClass = new DanceClass(ClassType.GROUP,
                                               true,
                                               BigDecimal.ONE,
                                               BigDecimal.ONE,
                                               BigDecimal.ONE);
        danceClassRepository.save(danceClass);

        return Booking.builder()
                      .user(user)
                      .room(Room.ASTAIRE)
                      .danceClass(danceClass)
                      .active(true)
                      .shareable(false)
                      .bookedFrom(LocalDateTime.of(2024, Month.MARCH, 30, 12, 45))
                      .bookedTo(LocalDateTime.of(2024, Month.MARCH, 30, 13, 15))
                      .totalPrice(BigDecimal.TEN)
                      .build();
    }
}