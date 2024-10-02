package com.springboot.counselor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@AllArgsConstructor
@Getter
@Setter
public class AvailableTimeDto {
    private long availableTimeId;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isReserved;
}
