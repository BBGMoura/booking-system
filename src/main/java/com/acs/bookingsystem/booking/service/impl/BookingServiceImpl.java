package com.acs.bookingsystem.booking.service.impl;

import com.acs.bookingsystem.booking.config.ScheduleConfig;
import com.acs.bookingsystem.booking.dto.BookingDTO;
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
import com.acs.bookingsystem.common.exception.ErrorCode;
import com.acs.bookingsystem.common.exception.RequestException;
import com.acs.bookingsystem.payment.PriceCalculator;
import com.acs.bookingsystem.user.dto.UserDTO;
import com.acs.bookingsystem.user.entities.User;
import com.acs.bookingsystem.user.mapper.UserMapper;
import com.acs.bookingsystem.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    ScheduleConfig scheduleConfig;

    @Override
    public BookingDTO createBooking(BookingRequest bookingRequest) {
        User user = getActiveUser(bookingRequest.getUserId());
        DanceClass danceClass = getDanceClass(bookingRequest.getClassType());

        validateBookingTime(bookingRequest);
        BigDecimal totalCost = PriceCalculator.calculateTotalPrice(bookingRequest.getDateFrom(), bookingRequest.getDateTo() , danceClass);

        Booking booking = new Booking(user,
                                      bookingRequest.getRoom(),
                                      danceClass,
                                      true,
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
        return bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(room, dateFrom, dateTo)
                                .stream()
                                .map(bookingMapper::mapBookingToDTO)
                                .toList();
    }

    @Override
    public void deleteBooking(int bookingId) {
        Booking booking = findBookingById(bookingId);
        booking.setActive(false);
        bookingRepository.save(booking);
    }

    private void validateBookingTime(BookingRequest bookingRequest){
        validateBookingStartsBeforeEnds(bookingRequest.getDateFrom(), bookingRequest.getDateTo());
        validateBookingDoesNotOverlapActiveExistingBookings(bookingRequest);
        validateBookingIsSameDate(bookingRequest.getDateFrom(), bookingRequest.getDateTo());
        validateBookingIsWithinOpeningTime(bookingRequest.getDateFrom(), bookingRequest.getDateTo());
    }

    private void validateBookingStartsBeforeEnds(LocalDateTime dateFrom, LocalDateTime dateTo) {
        if (dateFrom.isAfter(dateTo)) {
            throw new RequestException("Booking start time is after end time.", ErrorCode.INVALID_BOOKING_REQUEST);
        }
    }

    private void validateBookingDoesNotOverlapActiveExistingBookings(BookingRequest bookingRequest) {
        List<Booking> bookings = bookingRepository.findActiveBookingsByRoomAndEndOrStartBetweenTimeRange(bookingRequest.getRoom(),
                                                                                                   bookingRequest.getDateFrom(),
                                                                                                   bookingRequest.getDateTo());
        if (!bookings.isEmpty()) {
            // add log: System.out.println(String.format("Cannot make booking request %s as timeslot is unavailable", bookingRequest.toString()));
            throw new RequestException("Booking timeslot is unavailable.", ErrorCode.INVALID_BOOKING_REQUEST);
        }
    }

    private void validateBookingIsSameDate(LocalDateTime dateFrom, LocalDateTime dateTo) {
        if (!dateFrom.toLocalDate().equals(dateTo.toLocalDate())) {
            throw new RequestException("Booking must start and end on the same day.", ErrorCode.INVALID_BOOKING_REQUEST);
        }
    }

    private void validateBookingIsWithinOpeningTime(LocalDateTime dateFrom, LocalDateTime dateTo) {
        DayOfWeek dayOfWeek = dateFrom.getDayOfWeek();
        LocalTime openingTime;
        LocalTime closingTime;

        if (dayOfWeek == DayOfWeek.SATURDAY) {
            openingTime = scheduleConfig.getSaturdayOpening();
            closingTime = scheduleConfig.getSaturdayClosing();
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            openingTime = scheduleConfig.getSundayOpening();
            closingTime = scheduleConfig.getSundayClosing();
        } else {
            openingTime = scheduleConfig.getWeekdayOpening();
            closingTime = scheduleConfig.getWeekdayClosing();
        }

        validateTimeRange(dateFrom,openingTime,closingTime);
        validateTimeRange(dateTo,openingTime,closingTime);
    }

    private void validateTimeRange(LocalDateTime dateTime, LocalTime openingTime, LocalTime closingTime) {
        if (dateTime.toLocalTime().isBefore(openingTime) || dateTime.toLocalTime().isAfter(closingTime)) {
            throw new RequestException("Cannot make a booking as booking time is not within opening times.", ErrorCode.INVALID_BOOKING_REQUEST);
        }
    }

    private Booking findBookingById(int bookingId) {
        return bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new RequestException("Could not find booking with ID "+bookingId, ErrorCode.INVALID_BOOKING_ID));
    }

    private User getActiveUser(int id) {
        UserDTO userDto = userService.getActiveUserById(id);
        return userMapper.mapDTOToUser(userDto);
    }

    private DanceClass getDanceClass(ClassType classType) {
        DanceClassDTO danceClassDTO = danceClassService.getDanceClassByActiveClassType(classType);
        return danceClassMapper.mapDtoToDanceClass(danceClassDTO);
    }
}
