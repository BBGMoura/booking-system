package com.acs.bookingsystem.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.entity.BookingStatus;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.repository.BookingStatusRepository;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingStatusServiceTest {

  @Mock private BookingStatusRepository bookingStatusRepository;

  @InjectMocks private BookingStatusService bookingStatusService;

  private final User user =
      User.builder().id(1L).uid(UUID.randomUUID()).role(Role.ROLE_USER).build();
  private final Booking booking = Booking.builder().id(10L).uid(UUID.randomUUID()).build();

  @Test
  void save_persistsStatusEntityWithCorrectFields() {
    bookingStatusService.save(booking, user, BookingStatusType.BOOKED);

    ArgumentCaptor<BookingStatus> captor = ArgumentCaptor.forClass(BookingStatus.class);
    verify(bookingStatusRepository).save(captor.capture());
    BookingStatus saved = captor.getValue();
    assertThat(saved.getBooking()).isEqualTo(booking);
    assertThat(saved.getCreatedBy()).isEqualTo(user);
    assertThat(saved.getStatus()).isEqualTo(BookingStatusType.BOOKED);
  }

  @Test
  void resolveStatus_returnsStatusFromRepository() {
    when(bookingStatusRepository.findLatestStatusByBookingId(booking.getId()))
        .thenReturn(Optional.of(BookingStatusType.CANCELLED));

    BookingStatusType result = bookingStatusService.resolveStatus(booking);

    assertThat(result).isEqualTo(BookingStatusType.CANCELLED);
  }

  @Test
  void resolveStatus_defaultsToBooked_whenNoStatusExists() {
    when(bookingStatusRepository.findLatestStatusByBookingId(any())).thenReturn(Optional.empty());

    BookingStatusType result = bookingStatusService.resolveStatus(booking);

    assertThat(result).isEqualTo(BookingStatusType.BOOKED);
  }
}
