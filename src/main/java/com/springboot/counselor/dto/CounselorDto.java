package com.springboot.counselor.dto;

import com.springboot.counselor.entity.Career;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.entity.License;
import com.springboot.gender.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CounselorDto {
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Post{
        @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문 또는 숫자로만 입력해야 합니다. (4~20자)")
        private String userId;
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?=\\S+$).{10,20}", message = "비밀번호는 영문 대소문자와 숫자, 특수문자를 포함하여 10~20글자로 작성해야 합니다.")
        private String password;
        @Pattern(regexp = "^01([016789])-?([0-9]{3,4})-?([0-9]{4})$", message = "전화번호는 01x-xxxx-xxxx 형식과 일치해야 합니다")
        private String phone;
        private LocalDate birth;
        private Gender gender;
        @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z]{2,10}$", message = "이름은 영문 또는 한글로만 입력해야 합니다. (2~10자)")
        private String name;
        @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z ]{2,30}$", message = "회사명은 영문 또는 한글로만 입력해야 합니다. (2~30자)") // 공백도 허용
        private String currentCompany;
        @Pattern(regexp = "^01([016789])-?([0-9]{3,4})-?([0-9]{4})$", message = "전화번호는 01x-xxxx-xxxx 형식과 일치해야 합니다")
        private String currentCompanyTel;
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
        private int chatPrice;
        private int callPrice;
        private String profileImage;
        private String introduction;
        private String expertise;
        private String sessionDescription;
    }
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class DefaultDays{
        private DayOfWeek dayOfWeek;
        private List<LocalTime> times;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Response{
        private long counselorId;
        private LocalDate birth;
        private Gender gender;
        private Counselor.Status counselorStatus;
        private String name;
        private String userId;
        private String company;
        private List<AvailableDateDto.Response> availableDates;
        private int chatPrice;
        private int callPrice;
        private List<CareerDto.Response> careers;
        private List<LicenseDto.Response> licenses;
        private String introduction;
        private String expertise;
        private String sessionDescription;
        private String profileImage;
        private String rating;
        private int reviews;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
