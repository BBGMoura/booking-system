package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.BookingService;
import com.acs.bookingsystem.booking.view.BookingViewFactory;
import com.acs.bookingsystem.booking.view.ViewType;
import com.acs.bookingsystem.booking.view.dto.BookingView;
import com.acs.bookingsystem.security.CurrentUser;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/admin/bookings")
public class BookingAdminController {

    private final BookingService bookingService;
    private final BookingViewFactory viewFactory;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<BookingView> createBooking(@RequestParam UUID userUid,
                                                     @Valid @RequestBody BookingRequest bookingRequest) {
        User user = userService.getUserByUid(userUid);
        Booking booking = bookingService.createBooking(bookingRequest, user);
        return new ResponseEntity<>(viewFactory.createView(booking, ViewType.DETAIL), HttpStatus.CREATED);
    }

    @GetMapping("/{bookingUid}")
    public ResponseEntity<BookingView> getBookingByUid(@CurrentUser User user,
                                                       @PathVariable UUID bookingUid) {
        Booking booking = bookingService.getBookingByUid(bookingUid);
        return ResponseEntity.ok(viewFactory.createView(booking, user.getRole()));
    }

    @GetMapping("/user/{userUid}")
    public ResponseEntity<Page<BookingView>> getBookingsByUserUid(@PathVariable UUID userUid,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "5") int size) {
        Page<BookingView> bookings = bookingService.getAllBookingsByUserUid(userUid, page, size)
                                                   .map(b -> viewFactory.createView(b, ViewType.DETAIL));
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{bookingUid}")
    public ResponseEntity<Void> cancelBooking(@PathVariable UUID bookingUid) {
        bookingService.deactivateBooking(bookingUid);
        return ResponseEntity.noContent().build();
    }
}
