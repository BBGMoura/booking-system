package com.acs.bookingsystem.booking.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@Getter
@PropertySource("classpath:schedule.properties")
public class ScheduleConfig {
    @Value("#{T(java.time.LocalTime).parse('${weekday.opening.time}')}")
    LocalTime weekdayOpening;
    @Value("#{T(java.time.LocalTime).parse('${weekday.closing.time}')}")
    LocalTime weekdayClosing;
    @Value("#{T(java.time.LocalTime).parse('${saturday.opening.time}')}")
    LocalTime saturdayOpening;
    @Value("#{T(java.time.LocalTime).parse('${saturday.closing.time}')}")
    LocalTime saturdayClosing;
    @Value("#{T(java.time.LocalTime).parse('${sunday.opening.time}')}")
    LocalTime sundayOpening;
    @Value("#{T(java.time.LocalTime).parse('${sunday.closing.time}')}")
    LocalTime sundayClosing;
}
