package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.view.dto.BookingDetail;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.service.BookingService;
import com.acs.bookingsystem.booking.view.dto.BookingView;
import com.acs.bookingsystem.security.CurrentUser;
import com.acs.bookingsystem.user.entity.User;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/bookings")
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping()
    public ResponseEntity<BookingView> createBooking(@CurrentUser User user, @Valid @RequestBody BookingRequest bookingRequest) {
        return new ResponseEntity<>(bookingService.createBooking(bookingRequest, user.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingView> getBookingByBookingId(@CurrentUser User user, @PathVariable int bookingId) {
        // TODO: get booking by user and id
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @GetMapping()
    public ResponseEntity<Page<BookingView>> getBookingsByUserId(@CurrentUser User user,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(bookingService.getAllBookingsByUserId(user.getId(), page, size));
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<BookingDetail>> getBookingsByRoomAndTime(@RequestParam(name="room") Room room,
                                                                        @RequestParam(name="dateFrom") LocalDateTime dateFrom,
                                                                        @RequestParam(name="dateTo") LocalDateTime dateTo) {
        return ResponseEntity.ok(bookingService.getAllByRoomAndBetweenTwoDates(room, dateFrom, dateTo));
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelBooking(@CurrentUser User user, @PathVariable int id) {
        bookingService.deactivateBooking(id);
        return ResponseEntity.noContent().build();
    }
}
