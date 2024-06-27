package com.acs.bookingsystem.booking.service.impl;

import com.acs.bookingsystem.booking.config.ScheduleConfig;
import com.acs.bookingsystem.booking.dto.BookingDTO;
import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.entities.Booking;
import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.validation.BookingManager;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.booking.mapper.BookingMapper;
import com.acs.bookingsystem.booking.mapper.DanceClassMapper;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.DanceClassService;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.entities.User;
import com.acs.bookingsystem.user.enums.Permission;
import com.acs.bookingsystem.user.mapper.UserMapper;
import com.acs.bookingsystem.user.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.acs.bookingsystem.TestDataUtil.createUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private DanceClassService danceClassService;
    @Mock
    private DanceClassMapper danceClassMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private ScheduleConfig scheduleConfig;
    @Mock
    private BookingManager bookingManager;
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void testCreateBookingSuccessfully() {
        // Given
        LocalDateTime dateFrom = LocalDateTime.of(2024, 4, 21, 10, 0);
        LocalDateTime dateTo = LocalDateTime.of(2024, 4, 21, 11, 0);
        User user = createUser();
        DanceClass danceClass = createDanceClass();
        Booking booking = createBooking(user, danceClass, dateFrom, dateTo, new BigDecimal("100.00"));
        BookingDTO bookingDto = createBookingDTO(dateFrom, dateTo, new BigDecimal("100.00"));

        when(userService.getActiveUserById(1)).thenReturn(new UserDTO(1, "John", "Doe", "john.doe@example.com", "1234567890", true, Permission.USER));
        when(userMapper.mapDTOToUser(any(UserDTO.class))).thenReturn(user);
        when(danceClassService.getDanceClassByActiveClassType(ClassType.PRIVATE)).thenReturn(new DanceClassDTO(1, ClassType.PRIVATE, true, new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO));
        when(danceClassMapper.mapDtoToDanceClass(any(DanceClassDTO.class))).thenReturn(danceClass);
        when(bookingMapper.mapBookingToDTO(any(Booking.class))).thenReturn(bookingDto);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // When
        BookingRequest bookingRequest = new BookingRequest(1, Room.ASTAIRE, ClassType.PRIVATE, false, dateFrom, dateTo);
        BookingDTO result = bookingService.createBooking(bookingRequest);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.totalPrice());
    }

    @Test
    void testCreateBookingWithTimeOverlap() {
        // Arrange
        LocalDateTime dateFrom = LocalDateTime.of(2024, 4, 21, 10, 0);
        LocalDateTime dateTo = LocalDateTime.of(2024, 4, 21, 11, 0);
        User user = createUser();
        DanceClass danceClass = createDanceClass();
        Booking booking = createBooking(user, danceClass, dateFrom, dateTo, new BigDecimal("100.00"));

        // Act & Assert
        BookingRequest bookingRequest = new BookingRequest(1, Room.ASTAIRE, ClassType.PRIVATE, false, dateFrom, dateTo);
        when(bookingManager.validateBookingTime(bookingRequest)).thenReturn(Optional.of("Cannot place booking"));


        assertThrows(RequestException.class, () -> bookingService.createBooking(bookingRequest));
    }

    @Test
    void testCreateBookingOutsideOpeningHours() {
        // Arrange
        LocalDateTime dateFrom = LocalDateTime.of(2024, 4, 21, 7, 0); // Before opening hours
        LocalDateTime dateTo = LocalDateTime.of(2024, 4, 21, 8, 0);

        // Act & Assert
        BookingRequest bookingRequest = new BookingRequest(1, Room.ASTAIRE, ClassType.PRIVATE, false, dateFrom, dateTo);
        when(bookingManager.validateBookingTime(bookingRequest)).thenReturn(Optional.of("Booking ends before starts"));

        assertThrows(RequestException.class, () -> bookingService.createBooking(bookingRequest));
    }

    @Test
    void testCreateBookingEndsBeforeStarts_throwsException() {
        // Arrange
        LocalDateTime dateFrom = LocalDateTime.of(2024, 4, 21, 12, 0); // Before opening hours
        LocalDateTime dateTo = LocalDateTime.of(2024, 4, 21, 11, 0);

        // Act & Assert
        BookingRequest bookingRequest = new BookingRequest(1, Room.ASTAIRE, ClassType.PRIVATE, false, dateFrom, dateTo);

        when(bookingManager.validateBookingTime(bookingRequest)).thenReturn(Optional.of("Booking ends before starts"));
        assertThrows(RequestException.class, () -> bookingService.createBooking(bookingRequest));
    }

    @Test
    void testGetBookingByIdExisting() {
        // Arrange
        LocalDateTime dateFrom = LocalDateTime.of(2024, 4, 21, 10, 0);
        LocalDateTime dateTo = LocalDateTime.of(2024, 4, 21, 11, 0);
        User user = createUser();
        DanceClass danceClass = createDanceClass();
        Booking booking = createBooking(user, danceClass, dateFrom, dateTo, new BigDecimal("100.00"));
        BookingDTO bookingDto = createBookingDTO(dateFrom, dateTo, new BigDecimal("100.00"));

        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(bookingMapper.mapBookingToDTO(any(Booking.class))).thenReturn(bookingDto);

        // Act
        BookingDTO result = bookingService.getBookingById(1);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.totalPrice());
    }

    @Test
    void testGetBookingByIdNonExistent() {
        // Arrange
        when(bookingRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1));
    }

    @Test
    void testGetAllBookingsByUserWithMultipleBookings() {
        // Arrange
        User user = createUser();
        DanceClass danceClass = createDanceClass();
        Booking booking1 = createBooking(user, danceClass, LocalDateTime.of(2024, 4, 21, 10, 0), LocalDateTime.of(2024, 4, 21, 12, 0), new BigDecimal("100.00"));
        Booking booking2 = createBooking(user, danceClass, LocalDateTime.of(2024, 4, 22, 10, 0), LocalDateTime.of(2024, 4, 22, 12, 0), new BigDecimal("100.00"));
        List<Booking> bookings = List.of(booking1, booking2);
        when(bookingRepository.findAllByUserId(1)).thenReturn(bookings);
        when(bookingMapper.mapBookingToDTO(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            return createBookingDTO(b.getBookedFrom(), b.getBookedTo(), b.getTotalPrice());
        });

        // Act
        List<BookingDTO> results = bookingService.getAllBookingsByUser(1);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testDeactivateBookingSuccessfully() {
        // Arrange
        User user = createUser();
        DanceClass danceClass = createDanceClass();
        Booking booking = createBooking(user, danceClass, LocalDateTime.of(2024, 4, 21, 10, 0), LocalDateTime.of(2024, 4, 21, 12, 0), new BigDecimal("100.00"));
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        doAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            assertFalse(b.isActive()); // Check that the booking is marked as inactive
            return null;
        }).when(bookingRepository).save(any(Booking.class));

        // Act
        bookingService.deactivateBooking(1);

        // Assert
        verify(bookingRepository, times(1)).save(booking);
        assertFalse(booking.isActive());
    }

    @Test
    void testDeleteNonExistentBooking() {
        // Arrange
        when(bookingRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> bookingService.deactivateBooking(1));
    }


    private DanceClass createDanceClass() {
        BigDecimal pricePer60 = new BigDecimal("100.00");
        return new DanceClass(ClassType.PRIVATE, true, pricePer60, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private Booking createBooking(User user, DanceClass danceClass, LocalDateTime from, LocalDateTime to, BigDecimal price) {
        return Booking.builder()
                      .user(user)
                      .room(Room.ASTAIRE)
                      .danceClass(danceClass)
                      .active(true)
                      .shareable(false)
                      .bookedFrom(from)
                      .bookedTo(to)
                      .totalPrice(price)
                      .build();
    }

    private BookingDTO createBookingDTO(LocalDateTime from, LocalDateTime to, BigDecimal price) {
        return new BookingDTO(1, 1, Room.ASTAIRE, true, false, 1, from, to, price);
    }

}