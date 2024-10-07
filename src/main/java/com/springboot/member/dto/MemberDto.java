package com.springboot.member.dto;

import com.springboot.gender.Gender;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

public class MemberDto {
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Post{
        @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문 또는 숫자로만 입력해야 합니다. (4~20자)")
        private String userId;
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?=\\S+$).{10,20}", message = "비밀번호는 영문 대소문자와 숫자, 특수문자를 포함하여 10~20글자로 작성해야 합니다.")
        private String password;
        @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 영문 및 한글 또는 숫자로만 입력해야 합니다. (2~10자)")
        private String nickname;
        private LocalDate birth;
        private Gender gender;
        private String ci;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Patch{
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?=\\S+$).{10,20}", message = "비밀번호는 영문 대소문자와 숫자, 특수문자를 포함하여 10~20글자로 작성해야 합니다.")
        private String password;
        @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 영문 및 한글 또는 숫자로만 입력해야 합니다. (2~10자)")
        private String nickname;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Response{
        private long memberId;
        private String userId;
        private String nickname;
        private LocalDate birth;
        private Gender gender;
        private List<ReservationDto.Response> reservations;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    public static class FcmTokenDto {
        private String fcmToken;
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Test {
        private int score;
    }
}
