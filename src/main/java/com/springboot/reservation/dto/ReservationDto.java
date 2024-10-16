package com.springboot.reservation.dto;

import com.springboot.gender.Gender;
import com.springboot.reservation.entity.Report;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.entity.Review;
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
        private long counselorId;
        private String comment;
        private Reservation.CounselingType type;
        private LocalDate date;
        private List<LocalTime> startTimes;
        private int fee;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        private long reservationId;
        private long memberId;
        private long counselorId;
        private String memberNickname;
        private int memberBirthYear;
        private Gender memberGender;
        private String memberDepressionScore;
        private String counselorName;
        private String comment;
        private Reservation.CounselingType type;
        private Reservation.ReservationStatus status;
        private LocalDate date;
        private String startTime;
        private String endTime;
        private int fee;
        private String cancelComment;
        private ReviewDto.Response review;
        private ReportDto.Response report;
    }
}
