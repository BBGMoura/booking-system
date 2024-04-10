package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.dto.BookingDTO;
import com.acs.bookingsystem.booking.dto.BookingRequest;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.service.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins="localhost:8080")
@RequestMapping("/booking")
public class BookingController {
    BookingService bookingService;

    @PostMapping()
    public BookingDTO createBooking(@RequestBody BookingRequest bookingRequest){
        return bookingService.createBooking(bookingRequest);
    }

    @GetMapping("/{id}")
    public BookingDTO getBookingByBookingId(@PathVariable int id){
        return bookingService.getBookingById(id);
    }

    @GetMapping("/user/{userId}")
    public List<BookingDTO> getBookingsByUserId(@PathVariable int userId){
        return bookingService.getAllBookingsByUser(userId);
    }

    @GetMapping("/schedule")
    public List<BookingDTO> getBookingsByRoomAndTime(@RequestParam(name="room") Room room,
                                                     @RequestParam(name="dateFrom") LocalDateTime dateFrom,
                                                     @RequestParam(name="dateTo") LocalDateTime dateTo) {
        return bookingService.getAllByRoomAndBetweenTwoDates(room,dateFrom,dateTo);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteBooking(@PathVariable int id) {
        bookingService.deleteBooking(id);
    }

}
