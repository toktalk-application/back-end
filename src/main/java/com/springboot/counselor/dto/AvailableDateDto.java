package com.springboot.counselor.dto;

import com.springboot.counselor.available_date.AvailableTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class AvailableDateDto {
    private long availableDateId;
    private LocalDate date;
    private Map<LocalTime, AvailableTimeDto> availableTimes;
}
