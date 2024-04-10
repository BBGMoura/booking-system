package com.acs.bookingsystem.booking.service.impl;

import com.acs.bookingsystem.booking.dto.BookingDTO;
import com.acs.bookingsystem.booking.dto.BookingRequest;
import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.entities.Booking;
import com.acs.bookingsystem.booking.entities.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.mapper.BookingMapper;
import com.acs.bookingsystem.booking.mapper.DanceClassMapper;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.service.BookingService;
import com.acs.bookingsystem.booking.service.DanceClassService;
import com.acs.bookingsystem.common.exception.ErrorCode;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.payment.PriceCalculator;
import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.entities.User;
import com.acs.bookingsystem.user.mapper.UserMapper;
import com.acs.bookingsystem.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    BookingRepository bookingRepository;
    UserService userService;
    UserMapper userMapper;
    DanceClassService danceClassService;
    DanceClassMapper danceClassMapper;
    BookingMapper bookingMapper;

    @Override
    public BookingDTO createBooking(BookingRequest bookingRequest) {
        validateBookingTime(bookingRequest);

        User user = getUser(bookingRequest.getUserId());
        DanceClass danceClass = getDanceClass(bookingRequest.getClassType());
        BigDecimal totalCost = PriceCalculator.calculateTotalPrice(bookingRequest.getDateFrom(), bookingRequest.getDateTo() , danceClass);

        Booking booking = new Booking(user,
                                      bookingRequest.getRoom(),
                                      danceClass,
                                      bookingRequest.getDateFrom(),
                                      bookingRequest.getDateTo(),
                                      totalCost);

        return bookingMapper.mapBookingToDTO(bookingRepository.save(booking));
    }

    @Override
    public BookingDTO getBookingById(int bookingId) {
        return bookingMapper.mapBookingToDTO(findBookingById(bookingId));
    }

    @Override
    public List<BookingDTO> getAllBookingsByUser(int userId) {
        return bookingRepository.findAllByUserId(userId)
                                .stream()
                                .map(bookingMapper::mapBookingToDTO)
                                .toList();
    }

    @Override
    public List<BookingDTO> getAllByRoomAndBetweenTwoDates(Room room, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return bookingRepository.findBookingsByRoomAndEndOrStartBetweenTimeRange(room, dateFrom, dateTo)
                                .stream()
                                .map(bookingMapper::mapBookingToDTO)
                                .toList();
    }

    @Override
    public void deleteBooking(int bookingId) {
        bookingRepository.delete(findBookingById(bookingId));
    }

    private void validateBookingTime(BookingRequest bookingRequest){
        List<Booking> bookings = bookingRepository.findBookingsByRoomAndEndOrStartBetweenTimeRange(bookingRequest.getRoom(),
                                                                                                   bookingRequest.getDateFrom(),
                                                                                                   bookingRequest.getDateTo());
        if (!bookings.isEmpty()) {
            System.out.println(String.format("Cannot make booking request %s as timeslot is unavailable", bookingRequest.toString()));
            throw new RequestException("Booking timeslot is unavailable.", ErrorCode.INVALID_BOOKING_REQUEST);
        }
    }

    private Booking findBookingById(int bookingId) {
        return bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new RequestException(String.format("Could not find booking with ID: %d", bookingId), ErrorCode.INVALID_ID));
    }

    private User getUser(int id) {
        UserDTO userDto = userService.getUserById(id);
        return userMapper.mapDTOToUser(userDto);
    }

    private DanceClass getDanceClass(ClassType classType) {
        DanceClassDTO danceClassDTO = danceClassService.getDanceClassByActiveClassType(classType);
        return danceClassMapper.mapDtoToDanceClass(danceClassDTO);
    }

}
