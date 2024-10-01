package com.springboot.counselor.dto;

import com.springboot.gender.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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
    }
}
