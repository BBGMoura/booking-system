package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.service.BookingQueryService;
import com.acs.bookingsystem.booking.view.BookingViewFactory;
import com.acs.bookingsystem.booking.view.ViewType;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.BookingManagerService;
import com.acs.bookingsystem.booking.view.dto.BookingView;
import com.acs.bookingsystem.security.CurrentUser;
import com.acs.bookingsystem.user.entity.User;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class BookingAdminController {

    private final BookingManagerService bookingManagerService;
    private final BookingQueryService bookingQueryService;
    private final BookingViewFactory viewFactory;

    @PostMapping("/bookings")
    public ResponseEntity<BookingView> createBooking(@RequestParam(name="userId") int userId,
                                                     @Valid @RequestBody BookingRequest bookingRequest) {
        Booking booking = bookingManagerService.createBooking(bookingRequest, userId);
        return new ResponseEntity<>(viewFactory.createView(booking, ViewType.DETAIL), HttpStatus.CREATED);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingView> getBookingByBookingId(@CurrentUser User user, @PathVariable int bookingId) {
        Booking booking = bookingQueryService.getBookingById(bookingId);
        return ResponseEntity.ok(viewFactory.createView(booking, user.getRole()));
    }

    @GetMapping("/bookings/user/{userId}")
    public ResponseEntity<Page<BookingView>> getBookingsByUserId(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "5") int size,
                                                                 @PathVariable int userId) {
        Page<BookingView> bookings = bookingQueryService.getAllBookingsByUserId(userId, page, size)
                                                        .map(booking -> viewFactory.createView(booking, ViewType.DETAIL));

        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/bookings/cancel/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable int bookingId) {
        bookingManagerService.deactivateBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
