package com.acs.bookingsystem.booking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@ConfigurationProperties(prefix = "schedule")
@Getter
@Setter
public class ScheduleProperties {
    private DayTime weekday;
    private DayTime saturday;
    private DayTime sunday;

    @Getter
    @Setter
    public static class DayTime {
        private LocalTime opening;
        private LocalTime closing;
    }
}
