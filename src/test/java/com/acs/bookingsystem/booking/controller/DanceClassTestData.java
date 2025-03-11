package com.acs.bookingsystem.booking.controller;

import com.acs.bookingsystem.booking.dto.DanceClassDTO;
import com.acs.bookingsystem.booking.enums.ClassType;
import com.acs.bookingsystem.booking.request.DanceClassRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

public class DanceClassTestData {

    static List<ClassType> classTypes = List.of(ClassType.PRACTICE,
                                                 ClassType.PRIVATE,
                                                 ClassType.GROUP);

    static DanceClassDTO danceClass = new DanceClassDTO(1,
                                                         ClassType.PRIVATE,
                                                         true,
                                                         BigDecimal.ZERO);

    static String danceClassRequestJson = asJsonString(new DanceClassRequest(ClassType.PRIVATE,
                                                                             BigDecimal.ZERO));

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
