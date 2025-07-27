package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.view.dto.BookingDetail;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.BookingService;
import com.acs.bookingsystem.booking.view.dto.BookingView;
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

    private final BookingService bookingService;

    @PostMapping("/bookings")
    public ResponseEntity<BookingView> createBooking(@RequestParam(name="userId") int userId,
                                                     @Valid @RequestBody BookingRequest bookingRequest) {
        return new ResponseEntity<>(bookingService.createBooking(bookingRequest, userId), HttpStatus.CREATED);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingDetail> getBookingByBookingId(@PathVariable int bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @GetMapping("/bookings/user/{userId}")
    public ResponseEntity<Page<BookingDetail>> getBookingsByUserId(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "5") int size,
                                                                   @PathVariable int userId) {
        return ResponseEntity.ok(bookingService.getAllBookingsByUserId(userId, page, size));
    }

    @PatchMapping("/bookings/cancel/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable int bookingId) {
        bookingService.deactivateBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
