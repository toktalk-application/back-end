package com.springboot.reservation.dto;

import com.springboot.reservation.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ReservationDto {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post{
        private long memberId;
        private long counselorId;
        private String comment;
        private Reservation.CounselingType type;
        private LocalDate date;
        private List<LocalTime> startTimes;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        private long reservationId;
        private long counselorId;
        private String comment;
        private Reservation.CounselingType type;
        private Reservation.ReservationStatus status;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
