package com.acs.bookingsystem.booking.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acs.bookingsystem.booking.BookingWithStatus;
import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.enums.BookingStatusType;
import com.acs.bookingsystem.booking.view.dto.BookingDetail;
import com.acs.bookingsystem.booking.view.dto.BookingSummary;
import com.acs.bookingsystem.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingViewFactoryTest {

  @Mock private BookingMapper mapper;
  @Mock private BookingDetail bookingDetail;
  @Mock private BookingSummary bookingSummary;

  private BookingViewFactory factory;

  private BookingWithStatus bookingWithStatus;

  @BeforeEach
  void setup() {
    factory = new BookingViewFactory(mapper);
    bookingWithStatus = new BookingWithStatus(new Booking(), BookingStatusType.BOOKED);
  }

  @Test
  void givenViewTypeDetail_whenCreateView_thenDelegatesToMapDetail() {
    when(mapper.mapDetail(any(Booking.class), eq(BookingStatusType.BOOKED)))
        .thenReturn(bookingDetail);

    var result = factory.createView(bookingWithStatus, ViewType.DETAIL);

    assertThat(result).isEqualTo(bookingDetail);
    verify(mapper).mapDetail(any(Booking.class), eq(BookingStatusType.BOOKED));
  }

  @Test
  void givenViewTypeSummary_whenCreateView_thenDelegatesToMapSummary() {
    when(mapper.mapSummary(any(Booking.class), eq(BookingStatusType.BOOKED)))
        .thenReturn(bookingSummary);

    var result = factory.createView(bookingWithStatus, ViewType.SUMMARY);

    assertThat(result).isEqualTo(bookingSummary);
    verify(mapper).mapSummary(any(Booking.class), eq(BookingStatusType.BOOKED));
  }

  @Test
  void givenRoleAdmin_whenCreateView_thenProducesDetail() {
    when(mapper.mapDetail(any(Booking.class), eq(BookingStatusType.BOOKED)))
        .thenReturn(bookingDetail);

    var result = factory.createView(bookingWithStatus, Role.ROLE_ADMIN);

    assertThat(result).isEqualTo(bookingDetail);
    verify(mapper).mapDetail(any(Booking.class), eq(BookingStatusType.BOOKED));
  }

  @Test
  void givenRoleUser_whenCreateView_thenProducesSummary() {
    when(mapper.mapSummary(any(Booking.class), eq(BookingStatusType.BOOKED)))
        .thenReturn(bookingSummary);

    var result = factory.createView(bookingWithStatus, Role.ROLE_USER);

    assertThat(result).isEqualTo(bookingSummary);
    verify(mapper).mapSummary(any(Booking.class), eq(BookingStatusType.BOOKED));
  }
}
