package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.service.BookingQueryService;
import com.acs.bookingsystem.booking.service.BookingManagerService;
import com.acs.bookingsystem.booking.view.BookingViewFactory;
import com.acs.bookingsystem.booking.view.ViewType;
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

    private final BookingManagerService bookingManagerService;
    private final BookingQueryService bookingQueryService;
    private final BookingViewFactory viewFactory;

    @PostMapping()
    public ResponseEntity<BookingView> createBooking(@CurrentUser User user, @Valid @RequestBody BookingRequest bookingRequest) {
        Booking booking = bookingManagerService.createBooking(bookingRequest, user.getId());
        return new ResponseEntity<>(viewFactory.createView(booking, ViewType.DETAIL), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingView> getOwnBookingById(@CurrentUser User user, @PathVariable int id) {
        Booking booking = bookingQueryService.getOwnBookingById(id, user.getId());
        return ResponseEntity.ok(viewFactory.createView(booking, ViewType.DETAIL));
    }

    @GetMapping()
    public ResponseEntity<Page<BookingView>> getBookingsByUserId(@CurrentUser User user,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "5") int size) {
        Page<BookingView> bookings = bookingQueryService.getAllBookingsByUserId(user.getId(), page, size)
                                                        .map(booking -> viewFactory.createView(booking, ViewType.DETAIL));
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<BookingView>> getBookingSchedule(@CurrentUser User user,
                                                                @RequestParam(name = "room") Room room,
                                                                @RequestParam(name = "dateFrom") LocalDateTime dateFrom,
                                                                @RequestParam(name = "dateTo") LocalDateTime dateTo) {
        List<BookingView> bookings = bookingQueryService.getBookingsByRoomAndDates(room, dateFrom, dateTo)
                                                        .stream()
                                                        .map(booking -> viewFactory.createView(booking, user.getRole()))
                                                        .toList();
        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelBooking(@CurrentUser User user, @PathVariable int id) {
        bookingManagerService.deactivateOwnBooking(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
