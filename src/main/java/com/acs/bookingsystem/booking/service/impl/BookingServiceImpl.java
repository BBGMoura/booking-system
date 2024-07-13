package com.acs.bookingsystem.booking.service.impl;

import com.acs.bookingsystem.booking.dto.BookingDTO;
import com.acs.bookingsystem.booking.validation.BookingManager;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.booking.request.BookingRequest;
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
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.payment.PriceCalculator;
import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.entities.User;
import com.acs.bookingsystem.user.mapper.UserMapper;
import com.acs.bookingsystem.user.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    public static final Logger LOG = LoggerFactory.getLogger(BookingServiceImpl.class);
    BookingRepository bookingRepository;
    BookingManager bookingManager;
    UserService userService;
    UserMapper userMapper;
    DanceClassService danceClassService;
    DanceClassMapper danceClassMapper;
    BookingMapper bookingMapper;

    @Override
    public BookingDTO createBooking(BookingRequest bookingRequest) {
        LOG.debug("Creating booking with request: {}", bookingRequest);

        User user = getActiveUser(bookingRequest.userId());
        DanceClass danceClass = getDanceClass(bookingRequest.classType());

        bookingManager.validateBookingTime(bookingRequest)
                      .ifPresent(errorMessage -> {
                             throw new RequestException(errorMessage, ErrorCode.INVALID_BOOKING_REQUEST);
                         });


        BigDecimal totalCost = PriceCalculator.calculateTotalPrice(bookingRequest.dateFrom(), bookingRequest.dateTo(), danceClass);
        Booking booking = Booking.builder()
                                 .user(user)
                                 .room(bookingRequest.room())
                                 .danceClass(danceClass)
                                 .active(true)
                                 .shareable(bookingRequest.isShareable())
                                 .bookedFrom(bookingRequest.dateFrom())
                                 .bookedTo(bookingRequest.dateTo())
                                 .totalPrice(totalCost)
                                 .build();

        return bookingMapper.mapBookingToDTO(bookingRepository.save(booking));
    }

    @Override
    public BookingDTO getBookingById(int bookingId) {
        return bookingMapper.mapBookingToDTO(findBookingById(bookingId));
    }

    //TODO: implementation will be reconsidered as paging is required.
    @Override
    public List<BookingDTO> getAllBookingsByUser(int userId) {
        return bookingRepository.findAllByUserId(userId)
                                .stream()
                                .map(bookingMapper::mapBookingToDTO)
                                .toList();
    }


    @Override
    public List<BookingDTO> getAllByRoomAndBetweenTwoDates(Room room, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(room, null,dateFrom, dateTo)
                                .stream()
                                .map(bookingMapper::mapBookingToDTO)
                                .toList();
    }

    @Override
    public void deactivateBooking(int bookingId) {
        Booking booking = findBookingById(bookingId);
        booking.deactivate();
        bookingRepository.save(booking);
    }

    private Booking findBookingById(int bookingId) {
        return bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new NotFoundException("Could not find booking with ID "+bookingId, ErrorCode.INVALID_BOOKING_ID));
    }

    //TODO: update implementation as active user might not be necessary anymore, just get user
    private User getActiveUser(int id) {
        UserDTO userDto = userService.getActiveUserById(id);
        return userMapper.mapDTOToUser(userDto);
    }

    private DanceClass getDanceClass(ClassType classType) {
        DanceClassDTO danceClassDTO = danceClassService.getDanceClassByActiveClassType(classType);
        return danceClassMapper.mapDtoToDanceClass(danceClassDTO);
    }
}
