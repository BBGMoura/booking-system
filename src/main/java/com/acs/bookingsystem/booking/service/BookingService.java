package com.acs.bookingsystem.booking.service;

import com.acs.bookingsystem.booking.view.dto.BookingDetail;
import com.acs.bookingsystem.booking.view.dto.BookingView;
import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.entity.Booking;
import com.acs.bookingsystem.booking.entity.DanceClass;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.enums.Room;
import com.acs.bookingsystem.booking.view.ViewType;
import com.acs.bookingsystem.booking.view.BookingViewFactory;
import com.acs.bookingsystem.booking.mapper.DanceClassMapper;
import com.acs.bookingsystem.booking.repository.BookingRepository;
import com.acs.bookingsystem.booking.request.BookingRequest;
import com.acs.bookingsystem.common.exception.NotFoundException;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.common.exception.model.ErrorCode;
import com.acs.bookingsystem.payment.PriceCalculator;
import com.acs.bookingsystem.user.entity.User;
import com.acs.bookingsystem.user.enums.Role;
import com.acs.bookingsystem.user.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingService {

    public static final Logger LOG = LoggerFactory.getLogger(BookingService.class);

    BookingRepository bookingRepository;
    BookingValidator bookingValidator;
    UserService userService;
    DanceClassService danceClassService;
    DanceClassMapper danceClassMapper;
    BookingViewFactory viewFactory;

    public BookingView createBooking(BookingRequest bookingRequest, int userId) {
        LOG.debug("Creating booking with request: {} for user: {}", bookingRequest, userId);

        User user = userService.getUserById(userId);

        DanceClass danceClass = getDanceClass(bookingRequest.classType(), user.getRole());

        // TODO: fix up booking validator
        // USE DESIGN PATTERN to define different validators, go through every validator in a loop instead of all in one class.
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

        return viewFactory.createView(booking, ViewType.DETAIL);
    }

    // TODO: fix up dto situation - will do this using views
    // TODO:

    public BookingDetail getBookingById(int bookingId) {
        return bookingMapper.mapDetail(findBookingById(bookingId));
    }

    public Page<BookingDetail> getAllBookingsByUserId(int userId, int page, int size) {
       Pageable pageable = PageRequest.of(page, size);

       Page<Booking> bookingPage = bookingRepository.findAllByUserId(userId,pageable);

        List<BookingDetail> bookings = bookingPage.getContent()
                                                  .stream()
                                                  .map(bookingMapper::mapDetail)
                                                  .sorted(Comparator.comparing(BookingDetail::dateFrom).reversed())
                                                  .toList();

       return new PageImpl<>(bookings, pageable, bookingPage.getTotalElements());
    }

    // TODO: this doesnt need detailed view. summary will do just fine.
    public List<BookingDetail> getAllByRoomAndBetweenTwoDates(Room room, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(room, null,dateFrom, dateTo)
                                .stream()
                                .map(bookingMapper::mapDetail)
                                .toList();
    }

    public void deactivateBooking(int bookingId) {
        Booking booking = findBookingById(bookingId);
        booking.deactivate();
        bookingRepository.save(booking);
    }

    private Booking findBookingById(int bookingId) {
        return bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new NotFoundException("Could not find booking with ID " + bookingId, ErrorCode.INVALID_BOOKING_ID));
    }

    private DanceClass getDanceClass(ClassType classType, Role role) {
        DanceClassDTO danceClassDTO = danceClassService.getActiveDanceClass(classType, role);
        return danceClassMapper.mapDtoToDanceClass(danceClassDTO);
    }
}
