package com.acs.bookingsystem.booking;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.request.DanceClassRequest;

import java.math.BigDecimal;
import java.util.List;

public class DanceClassTestData {

    public static final List<ClassType> classTypes = List.of(ClassType.PRACTICE,
                                                       ClassType.PRIVATE,
                                                       ClassType.GROUP);

    public static final DanceClassDTO danceClass = new DanceClassDTO(1,
                                                               ClassType.PRIVATE,
                                                               true,
                                                               BigDecimal.ZERO);

    public static final DanceClassRequest danceClassRequest = new DanceClassRequest(ClassType.PRIVATE,
                                                                                    BigDecimal.ZERO);


}
