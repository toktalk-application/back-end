package com.springboot.counselor.dto;

import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.counselor.entity.Career;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.entity.License;
import com.springboot.counselor.service.CounselorService;
import com.springboot.gender.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CounselorDto {
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Post{
        private String userId;
        private String password;
        private String phone;
        private LocalDate birth;
        private Gender gender;
        private String ci;
        private String name;
        private String company;
        private List<License> licenses;
        private List<Career> careers;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Patch{
        private String password;
        private String phone;
        private String company;
        private String name;
        private List<License> licenses;
        private List<Career> careers;
        private int chatPrice;
        private int callPrice;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Response{
        private long counselorId;
        private LocalDate birth;
        private Gender gender;
        private Counselor.CounselorStatus counselorStatus;
        private String name;
        private String userId;
        private String company;
        private List<AvailableDateDto> availableDates;
        private int chatPrice;
        private int callPrice;
        private List<CareerDto.Response> careers;
        private List<LicenseDto.Response> licenses;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
