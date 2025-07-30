package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.service.validation.BookingValidator;
import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.entity.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.mapper.DanceClassMapper;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.payment.PriceCalculator;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class BookingManagerService {

    public static final Logger LOG = LoggerFactory.getLogger(BookingManagerService.class);

    BookingRepository bookingRepository;
    BookingValidator bookingValidator;
    BookingQueryService queryService;
    UserService userService;
    DanceClassService danceClassService;
    DanceClassMapper danceClassMapper;

    @Transactional
    public Booking createBooking(BookingRequest bookingRequest, int userId) {
        LOG.debug("Creating booking with request: {} for user: {}", bookingRequest, userId);

        User user = userService.getUserById(userId);
        DanceClass danceClass = getDanceClass(bookingRequest.classType(), user.getRole());

        bookingValidator.validate(bookingRequest)
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

        return bookingRepository.save(booking);
    }

    @Transactional
    public void deactivateOwnBooking(int bookingId, int userId) {
        Booking booking =  queryService.getOwnBookingById(bookingId, userId);
        booking.deactivate();
        bookingRepository.save(booking);
    }

    @Transactional
    public void deactivateBooking(int bookingId) {
        Booking booking = queryService.getBookingById(bookingId);
        booking.deactivate();
        bookingRepository.save(booking);
    }

    // TODO: Don't want the service to return a dto, returning a entity is fine.

    private DanceClass getDanceClass(ClassType classType, Role role) {
        DanceClassDTO danceClassDTO = danceClassService.getActiveDanceClass(classType, role);
        return danceClassMapper.mapDtoToDanceClass(danceClassDTO);
    }
}
