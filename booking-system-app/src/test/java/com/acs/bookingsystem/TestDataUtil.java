//package com.acs.bookingsystem;
//
//
//import com.acs.bookingsystem.booking.dto.BookingDTO;
//import com.acs.bookingsystem.booking.dto.DanceClassDTO;
//import com.acs.bookingsystem.booking.entities.DanceClass;
//import com.acs.bookingsystem.danceclass.enums.ClassType;
//import com.acs.bookingsystem.booking.enums.Room;
//import com.acs.bookingsystem.booking.request.BookingRequest;
//import com.acs.bookingsystem.danceclass.request.DanceClassRequest;
//
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//public class TestDataUtil {
//    public static userOld createTestUser() {
//        return new User("John", "Doe", "john@example.com", "01234567890");
//    }
//
//    public static UserDTO createUserDTO() {
//        return new UserDTO(1, "John", "Doe", "john@example.com", "01234567890");
//    }
//
//    public static UserRequest createUserRegistrationRequest() {
//        return new UserRequest("John", "Doe", "john@example.com", "01234567890");
//    }
//
//    public static UserUpdateRequest createUserUpdateRequest() {
//        return new UserUpdateRequest("John", "Doe", "01234567890");
//    }
//
//    public static DanceClass createDanceClass(ClassType classType, boolean isActive) {
//        return new DanceClass(classType, isActive, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
//    }
//
//    public static DanceClassDTO createDanceClassDTO(ClassType classType, boolean isActive) {
//        return new DanceClassDTO(1, classType, isActive, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
//    }
//
//    public static DanceClassRequest createDanceClassRequest(ClassType classType) {
//        return new DanceClassRequest(classType, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
//    }
//
//    public static BookingRequest createBookingRequest() {
//        LocalDateTime from = LocalDateTime.of(2024, 4, 20, 10, 0);
//        LocalDateTime to = LocalDateTime.of(2024, 4, 20, 12, 0);
//        return new BookingRequest(1, Room.ASTAIRE, ClassType.PRACTICE, false, from, to);
//    }
//
//    public static BookingDTO createBookingDTO() {
//        LocalDateTime from = LocalDateTime.of(2024, 4, 20, 10, 0);
//        LocalDateTime to = LocalDateTime.of(2024, 4, 20, 12, 0);
//        return new BookingDTO(1, 1, Room.ASTAIRE, true, false, 1, from, to, new BigDecimal("100.00"));
//    }
//}
