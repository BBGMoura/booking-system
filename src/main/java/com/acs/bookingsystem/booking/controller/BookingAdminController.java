package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.dto.BookingDTO;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class BookingAdminController {

    private final BookingService bookingService;

    @PostMapping("/bookings")
    public ResponseEntity<BookingDTO> createBooking(@RequestParam(name="userId") int userId,
                                                    @Valid @RequestBody BookingRequest bookingRequest) {
        return new ResponseEntity<>(bookingService.createBooking(bookingRequest, userId), HttpStatus.CREATED);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingDTO> getBookingByBookingId(@PathVariable int bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    // TODO: Return pageable booking object
    @GetMapping("/bookings/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUserId(@PathVariable int userId) {
        return ResponseEntity.ok(bookingService.getAllBookingsByUser(userId));
    }

    @PatchMapping("/bookings/cancel/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable int bookingId) {
        bookingService.deactivateBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
