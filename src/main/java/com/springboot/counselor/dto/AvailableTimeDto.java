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
    private String startTime;
    private String endTime;
    private boolean isReserved;
}
