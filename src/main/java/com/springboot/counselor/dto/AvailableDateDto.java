package com.springboot.counselor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.N;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class AvailableDateDto{
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch{
        private LocalDate date;
        private List<LocalTime> times;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Response {
        private long availableDateId;
        private LocalDate date;
        private Map<LocalTime, AvailableTimeDto> availableTimes;
    }
}
