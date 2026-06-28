package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.BookingWithStatus;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.BookingService;
import com.acs.bookingsystem.booking.view.BookingViewFactory;
import com.acs.bookingsystem.booking.view.ViewType;
import com.acs.bookingsystem.booking.view.dto.BookingView;
import com.acs.bookingsystem.security.CurrentUser;
import com.acs.bookingsystem.user.entity.User;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("java:S4684") // @CurrentUser resolves from security context, not request body
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/bookings")
@Validated
public class BookingController {

  private final BookingService bookingService;
  private final BookingViewFactory viewFactory;

  @PostMapping
  public ResponseEntity<BookingView> createBooking(
      @CurrentUser User user, @Valid @RequestBody BookingRequest bookingRequest) {
    BookingWithStatus result = bookingService.createBooking(bookingRequest, user);
    return new ResponseEntity<>(
        viewFactory.createView(result, ViewType.DETAIL), HttpStatus.CREATED);
  }

  @GetMapping("/{bookingUid}")
  public ResponseEntity<BookingView> getOwnBookingByUid(
      @CurrentUser User user, @PathVariable UUID bookingUid) {
    BookingWithStatus result = bookingService.getBookingByUidAndUser(bookingUid, user.getId());
    return ResponseEntity.ok(viewFactory.createView(result, ViewType.DETAIL));
  }

  @GetMapping
  public ResponseEntity<Page<BookingView>> getBookings(
      @CurrentUser User user,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size) {
    Page<BookingView> bookings =
        bookingService
            .getAllBookingsByUserId(user.getId(), page, size)
            .map(b -> viewFactory.createView(b, ViewType.DETAIL));
    return ResponseEntity.ok(bookings);
  }

  @GetMapping("/schedule")
  public ResponseEntity<List<BookingView>> getBookingSchedule(
      @CurrentUser User user,
      @RequestParam Room room,
      @RequestParam OffsetDateTime dateFrom,
      @RequestParam OffsetDateTime dateTo) {
    List<BookingView> bookings =
        bookingService.getBookingsByRoomAndDates(room, dateFrom, dateTo).stream()
            .map(b -> viewFactory.createView(b, user.getRole()))
            .toList();
    return ResponseEntity.ok(bookings);
  }

  @DeleteMapping("/{bookingUid}")
  public ResponseEntity<Void> cancelBooking(@CurrentUser User user, @PathVariable UUID bookingUid) {
    bookingService.cancelBookingByUserId(bookingUid, user.getId());
    return ResponseEntity.noContent().build();
  }
}
