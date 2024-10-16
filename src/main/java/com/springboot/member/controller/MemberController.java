package com.springboot.member.controller;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.counselor.entity.Counselor;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.firebase.dto.FcmTokenRequestDto;
import com.springboot.member.dto.DailyMoodDto;
import com.springboot.member.entity.DailyMood;
import com.springboot.response.SingleResponseDto;
import com.springboot.response.SingleResponseEntity;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import com.springboot.member.mapper.MemberMapper;
import com.springboot.member.service.MemberService;
import com.springboot.utils.CredentialUtil;
import com.springboot.utils.UriCreator;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/members")
@AllArgsConstructor
@Validated
public class MemberController {
    private final String MEMBER_DEFAULT_URL = "/members";
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    // 회원가입
    @PostMapping
    public ResponseEntity<?> postMember(@RequestBody @Valid MemberDto.Post postDto){
        Member tempMember = memberMapper.memberPostDtoToMember(postDto);
        Member member = memberService.createMember(tempMember);
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, member.getMemberId());

        return ResponseEntity.created(location).build();
    }

    // 아이디 중복 체크
    @GetMapping("/userid-availabilities")
    public ResponseEntity<?> checkUsernameAvailability(@RequestParam String userId) {
        return new ResponseEntity<>(
                new SingleResponseDto<>(memberService.isUserIdAvailable(userId)), HttpStatus.OK
        );
    }

    // 닉네임 중복 체크
    @GetMapping("/nickname-availabilities")
    public ResponseEntity<?> checkNicknameAvailability(@RequestParam String nickname){
        return new ResponseEntity<>(
                new SingleResponseDto<>(memberService.isNicknameAvailable(nickname)), HttpStatus.OK
        );
    }

    // 특정 회원 조회
    @GetMapping("/{memberId}")
    public ResponseEntity<?> getMember(@PathVariable long memberId,
                                       Authentication authentication){
        Member findMember = memberService.findMember(memberId);
        /*return new SingleResponseEntity<>(memberMapper.memberToMemberResponseDto(findMember), HttpStatus.OK);*/
        return new ResponseEntity<>(
                new SingleResponseDto<>(memberMapper.memberToMemberResponseDto(findMember)), HttpStatus.OK
        );
    }

    // 회원 프로필 수정
    @PatchMapping
    public ResponseEntity<?> patchMember(Authentication authentication,
                                         @RequestBody MemberDto.Patch patchDto){
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;
        LoginDto.UserType userType = auth.getUserType();
        // Member만 이용 가능한 요청
        if(!userType.equals(LoginDto.UserType.MEMBER)) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        Map<String, String> credentials = (Map<String, String>)(authentication.getCredentials());

        Member member = memberMapper.memberPatchDtoToMember(patchDto);
        long memberId = Long.parseLong(credentials.get("memberId"));
        member.setMemberId(memberId);

        // 서비스 로직 실행
        Member patchedMember = memberService.updateMember(member);

        return new ResponseEntity<>(
                new SingleResponseDto<>(memberMapper.memberToMemberResponseDto(patchedMember)), HttpStatus.OK
        );
    }

    // 오늘의 기분 등록
    @PostMapping("/daily-moods")
    public ResponseEntity<?> postDailyMood(Authentication authentication,
                                           @RequestBody DailyMoodDto.Post postDto){
        // 회원만 요청 가능
        LoginDto.UserType userType = CredentialUtil.getUserType(authentication);
        if(!userType.equals(LoginDto.UserType.MEMBER)) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        long memberId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"));
        memberService.addDailyMood(memberId, memberMapper.dailyMoodPostDtoToDailyMood(postDto));

        return new ResponseEntity<>(
                new SingleResponseDto<>(null), HttpStatus.CREATED
        );
    }

    // 오늘의 기분 월별 조회
    @GetMapping("/daily-moods")
    public ResponseEntity<?> getMonthlyMoods(Authentication authentication,
                                             @RequestParam @DateTimeFormat(pattern = "yyyy-mm") YearMonth month){
        // 회원만 요청 가능
        LoginDto.UserType userType = CredentialUtil.getUserType(authentication);
        if(!userType.equals(LoginDto.UserType.MEMBER)) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        long memberId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"));
        Map<LocalDate, DailyMood> monthlyMoods = memberService.getMonthlyMoods(memberId, month);

        return new ResponseEntity<>(
                new SingleResponseDto<>(memberMapper.dailyMoodMapToDailyMoodResponseDtoMap(monthlyMoods)), HttpStatus.OK
        );
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<?> updateFcmToken(@RequestBody MemberDto.FcmTokenDto fcmTokenDto,
                                            Authentication authentication) {
        try {
            System.out.println("FCM 토큰 업데이트 요청 받음");

            String userId = authentication.getName();
            System.out.println("인증된 사용자 ID: " + userId);

            long authenticatedMemberId = memberService.getMemberIdByUserId(userId);
            System.out.println("조회된 memberId: " + authenticatedMemberId);

            memberService.updateFcmToken(authenticatedMemberId, fcmTokenDto.getFcmToken());
            System.out.println("FCM 토큰 업데이트 성공");

            return ResponseEntity.ok("FCM token updated successfully");
        } catch (Exception e) {
            System.out.println("FCM 토큰 업데이트 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating FCM token: " + e.getMessage());
        }
    }

    // 남은 상담 건수 조회
    @GetMapping("/reservation-counts")
    public ResponseEntity<?> getReservationCounts(Authentication authentication){
        long memberId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"));

        int count = memberService.getReservationCounts(memberId);

        return new ResponseEntity<>(
                new SingleResponseDto<>(count), HttpStatus.OK
        );
    }

    // 회원탈퇴
    @DeleteMapping
    public ResponseEntity<?> quitMember(Authentication authentication){
        long memberId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"));
        memberService.quitMember(memberId);
        return new ResponseEntity<>(
                new SingleResponseDto<>(null), HttpStatus.OK
        );
    }


//    @PutMapping("/{userId}/fcm-token")
//    public ResponseEntity<String> updateFcmToken(
//            @PathVariable Long userId,
//            @RequestBody FcmTokenRequestDto requestDto,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        // 인증된 사용자와 요청의 userId가 일치하는지 확인
//        if (!userDetails.getUsername().equals(userId.toString())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
//        }
//
//        try {
//            memberService.updateFcmToken(userId, requestDto.getFcmToken());
//            return ResponseEntity.ok("FCM token updated successfully");
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating FCM token");
//        }
//    }
}
