package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.validation.BookingValidator;
import com.acs.bookingsystem.booking.service.validation.ValidationFailure;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.danceclass.entity.DanceClass;
import com.acs.bookingsystem.danceclass.enums.ClassType;
import com.acs.bookingsystem.danceclass.service.DanceClassService;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingValidator bookingValidator;
    @Mock private DanceClassService danceClassService;

    @InjectMocks private BookingService bookingService;

    private static final UUID BOOKING_UID = UUID.randomUUID();
    private static final int USER_ID = 1;

    private final User user = User.builder()
            .id(USER_ID)
            .uid(UUID.randomUUID())
            .email("test@example.com")
            .role(Role.ROLE_USER)
            .build();

    private final DanceClass danceClass = new DanceClass(1, ClassType.PRIVATE, true, BigDecimal.TEN, Role.ROLE_USER);

    private final BookingRequest request = new BookingRequest(
            Room.ASTAIRE, ClassType.PRIVATE, false,
            LocalDateTime.of(2025, 6, 2, 10, 0),
            LocalDateTime.of(2025, 6, 2, 11, 0));

    private final Booking booking = Booking.builder().uid(BOOKING_UID).active(true).build();

    // --- createBooking ---

    @Test
    void givenValidRequest_whenCreateBooking_thenSavesAndReturnsBooking() {
        when(danceClassService.getActiveDanceClass(ClassType.PRIVATE, Role.ROLE_USER)).thenReturn(danceClass);
        when(bookingValidator.validate(request)).thenReturn(Optional.empty());
        when(bookingRepository.save(any())).thenReturn(booking);

        Booking result = bookingService.createBooking(request, user);

        assertThat(result).isEqualTo(booking);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void givenValidationFailure_whenCreateBooking_thenThrowsRequestException() {
        when(danceClassService.getActiveDanceClass(ClassType.PRIVATE, Role.ROLE_USER)).thenReturn(danceClass);
        when(bookingValidator.validate(request)).thenReturn(
                Optional.of(new ValidationFailure("Time invalid", ErrorCode.BOOKING_TIME_INVALID)));

        assertThatThrownBy(() -> bookingService.createBooking(request, user))
                .isInstanceOf(RequestException.class)
                .hasMessageContaining("Time invalid");

        verify(bookingRepository, never()).save(any());
    }

    // --- deactivateOwnBooking ---

    @Test
    void givenValidUidAndUser_whenDeactivateOwnBooking_thenDeactivatesAndSaves() {
        Booking activeBooking = Booking.builder().uid(BOOKING_UID).active(true).build();
        when(bookingRepository.findByUidAndUserId(BOOKING_UID, USER_ID)).thenReturn(Optional.of(activeBooking));

        bookingService.deactivateOwnBooking(BOOKING_UID, USER_ID);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void givenBookingNotOwnedByUser_whenDeactivateOwnBooking_thenThrowsNotFoundException() {
        when(bookingRepository.findByUidAndUserId(BOOKING_UID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deactivateOwnBooking(BOOKING_UID, USER_ID))
                .isInstanceOf(NotFoundException.class);

        verify(bookingRepository, never()).save(any());
    }

    // --- deactivateBooking ---

    @Test
    void givenValidUid_whenDeactivateBooking_thenDeactivatesAndSaves() {
        Booking activeBooking = Booking.builder().uid(BOOKING_UID).active(true).build();
        when(bookingRepository.findByUid(BOOKING_UID)).thenReturn(Optional.of(activeBooking));

        bookingService.deactivateBooking(BOOKING_UID);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void givenUnknownUid_whenDeactivateBooking_thenThrowsNotFoundException() {
        when(bookingRepository.findByUid(BOOKING_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deactivateBooking(BOOKING_UID))
                .isInstanceOf(NotFoundException.class);
    }

    // --- deactivateAllBookingsByUserId ---

    @Test
    void givenUserId_whenDeactivateAll_thenCallsRepository() {
        when(bookingRepository.deactivateBookingsByUserId(USER_ID)).thenReturn(3);

        bookingService.deactivateAllBookingsByUserId(USER_ID);

        verify(bookingRepository).deactivateBookingsByUserId(USER_ID);
    }

    // --- getBookingByUid ---

    @Test
    void givenValidUid_whenGetBookingByUid_thenReturnsBooking() {
        when(bookingRepository.findByUid(BOOKING_UID)).thenReturn(Optional.of(booking));

        Booking result = bookingService.getBookingByUid(BOOKING_UID);

        assertThat(result.getUid()).isEqualTo(BOOKING_UID);
    }

    @Test
    void givenUnknownUid_whenGetBookingByUid_thenThrowsNotFoundException() {
        when(bookingRepository.findByUid(BOOKING_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingByUid(BOOKING_UID))
                .isInstanceOf(NotFoundException.class);
    }

    // --- getOwnBookingByUid ---

    @Test
    void givenValidUidAndOwner_whenGetBookingByUid_thenReturnsBookingAndUser() {
        when(bookingRepository.findByUidAndUserId(BOOKING_UID, USER_ID)).thenReturn(Optional.of(booking));

        Booking result = bookingService.getBookingByUidAndUser(BOOKING_UID, USER_ID);

        assertThat(result.getUid()).isEqualTo(BOOKING_UID);
    }

    @Test
    void givenUidNotOwnedByUser_whenGetBookingByUid_AndUser_thenThrowsNotFoundException() {
        when(bookingRepository.findByUidAndUserId(BOOKING_UID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingByUidAndUser(BOOKING_UID, USER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    // --- getAllBookingsByUserId ---

    @Test
    void givenUserId_whenGetAllBookingsByUserId_thenReturnsPaginatedResults() {
        Page<Booking> page = new PageImpl<>(List.of(booking));
        when(bookingRepository.findAllByUserId(eq(USER_ID), any(Pageable.class))).thenReturn(page);

        Page<Booking> result = bookingService.getAllBookingsByUserId(USER_ID, 0, 5);

        assertThat(result.getContent()).hasSize(1);
    }

    // --- getBookingsByRoomAndDates ---

    @Test
    void givenRoomAndDates_whenGetBookingsByRoomAndDates_thenReturnsBookings() {
        LocalDateTime from = LocalDateTime.of(2025, 6, 2, 10, 0);
        LocalDateTime to = LocalDateTime.of(2025, 6, 2, 11, 0);
        when(bookingRepository.findActiveBookingsForRoomAndTimeRange(eq(Room.ASTAIRE), eq(null), eq(from), eq(to)))
                .thenReturn(List.of(booking));

        List<Booking> result = bookingService.getBookingsByRoomAndDates(Room.ASTAIRE, from, to);

        assertThat(result).hasSize(1);
    }
}
