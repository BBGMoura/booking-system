package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.dto.BookingDTO;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins="localhost:8080")
@RequestMapping("/booking")
@Validated
public class BookingController {
    BookingService bookingService;

    @PostMapping()
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody BookingRequest bookingRequest){
        return new ResponseEntity<>(bookingService.createBooking(bookingRequest), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingByBookingId(@PathVariable int id){
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUserId(@PathVariable int userId){
        return ResponseEntity.ok(bookingService.getAllBookingsByUser(userId));
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<BookingDTO>> getBookingsByRoomAndTime(@RequestParam(name="room") Room room,
                                                              @RequestParam(name="dateFrom") LocalDateTime dateFrom,
                                                              @RequestParam(name="dateTo") LocalDateTime dateTo) {
        return ResponseEntity.ok(bookingService.getAllByRoomAndBetweenTwoDates(room,dateFrom,dateTo));
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable int id) {
        bookingService.deactivateBooking(id);
        return ResponseEntity.noContent().build();
    }
}
