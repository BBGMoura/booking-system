package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.BookingWithStatus;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.BookingService;
import com.acs.bookingsystem.booking.view.BookingViewFactory;
import com.acs.bookingsystem.booking.view.ViewType;
import com.acs.bookingsystem.booking.view.dto.BookingView;
import com.acs.bookingsystem.security.CurrentUser;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/admin/bookings")
public class BookingAdminController {

  private final BookingService bookingService;
  private final BookingViewFactory viewFactory;
  private final UserService userService;

  @PostMapping
  public ResponseEntity<BookingView> createBooking(
      @RequestParam UUID userUid, @Valid @RequestBody BookingRequest bookingRequest) {
    User user = userService.getUserByUid(userUid);
    BookingWithStatus result = bookingService.createBooking(bookingRequest, user);
    return new ResponseEntity<>(
        viewFactory.createView(result, ViewType.DETAIL), HttpStatus.CREATED);
  }

  @GetMapping("/{bookingUid}")
  public ResponseEntity<BookingView> getBookingByUid(
      @CurrentUser User user, @PathVariable UUID bookingUid) {
    BookingWithStatus result = bookingService.getBookingByUid(bookingUid);
    return ResponseEntity.ok(viewFactory.createView(result, user.getRole()));
  }

  @GetMapping("/user/{userUid}")
  public ResponseEntity<Page<BookingView>> getBookingsByUserUid(
      @PathVariable UUID userUid,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size) {
    Page<BookingView> bookings =
        bookingService
            .getAllBookingsByUserUid(userUid, page, size)
            .map(b -> viewFactory.createView(b, ViewType.DETAIL));
    return ResponseEntity.ok(bookings);
  }

  @DeleteMapping("/{bookingUid}")
  public ResponseEntity<Void> cancelBooking(
      @CurrentUser User admin, @PathVariable UUID bookingUid) {
    bookingService.cancelBooking(bookingUid, admin);
    return ResponseEntity.noContent().build();
  }
}
