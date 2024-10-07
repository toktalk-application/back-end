package com.springboot.counselor.controller;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.counselor.dto.AvailableDateDto;
import com.springboot.counselor.dto.CareerDto;
import com.springboot.counselor.dto.CounselorDto;
import com.springboot.counselor.dto.LicenseDto;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.mapper.CounselorMapper;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.mapper.ReservationMapper;
import com.springboot.reservation.service.ReservationService;
import com.springboot.response.SingleResponseDto;
import com.springboot.utils.CredentialUtil;
import com.springboot.utils.UriCreator;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/counselors")
@AllArgsConstructor
@Validated
public class CounselorController {
    private final String DEFAULT_URL = "/counselors";
    private final CounselorMapper counselorMapper;
    private final CounselorService counselorService;
    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    // 상담사 회원 가입
    @PostMapping
    public ResponseEntity<?> postCounselor(@RequestBody @Valid CounselorDto.Post postDto){
        Counselor tempCounselor = counselorMapper.counselorPostDtoToCounselor(postDto);
        Counselor savedCounselor = counselorService.createCounselor(tempCounselor, postDto);

        URI location = UriCreator.createUri(DEFAULT_URL, savedCounselor.getCounselorId());
        return ResponseEntity.created(location).build();
    }
    // 자격증 추가 등록
    @PostMapping("/licenses")
    public ResponseEntity<?> postLicenses(@RequestBody List<LicenseDto.Post> postDtos,
                                         Authentication authentication){
        // Counselor만 요청 가능
        if(!CredentialUtil.getUserType(authentication).equals(LoginDto.UserType.COUNSELOR)) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        long counselorId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"));
        counselorService.addLicense(counselorId, postDtos);

        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }
    // 자격증 삭제
    @DeleteMapping("/licenses")
    public ResponseEntity<?> deleteLicenses(@RequestParam int licenseNumber,
                                            Authentication authentication){
        // Counselor만 요청 가능
        if(!CredentialUtil.getUserType(authentication).equals(LoginDto.UserType.COUNSELOR)) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        long counselorId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"));
        counselorService.deleteLicense(counselorId, licenseNumber);

        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    // 경력사항 추가 등록
    @PostMapping("/careers")
    public ResponseEntity<?> postCareers(@RequestBody List<CareerDto.Post> postDtos,
                                         Authentication authentication){
        // Counselor만 요청 가능
        if(!CredentialUtil.getUserType(authentication).equals(LoginDto.UserType.COUNSELOR)) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        long counselorId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"));
        counselorService.addCareer(counselorId, postDtos);

        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }
    // 경력사항 삭제
    @DeleteMapping("/careers")
    public ResponseEntity<?> deleteCareers(@RequestParam int careerNumber,
                                            Authentication authentication){
        // Counselor만 요청 가능
        if(!CredentialUtil.getUserType(authentication).equals(LoginDto.UserType.COUNSELOR)) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        long counselorId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"));
        counselorService.deleteCareer(counselorId, careerNumber);

        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    // 특정 상담사에 대한 특정일 또는 월별 예약 정보 조회
    @GetMapping("/{counselorId}/reservations")
    public ResponseEntity<?> getReservations(/*Authentication authentication,*/
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-mm") YearMonth month,
                                                                  @PathVariable @Positive long counselorId){
        // 상담사 찾아오기
        Counselor counselor = counselorService.findCounselor(counselorId);

        if(date != null){ // date 파라미터를 넣었으면 특정일 조회
            List<Reservation> dailyReservations = reservationService.getDailyReservations(counselor, date);
            List<String> counselorNames = dailyReservations.stream()
                    .map(reservation -> counselorService.findCounselor(reservation.getCounselorId()).getName())
                    .collect(Collectors.toList());

            return new ResponseEntity<>(
                    new SingleResponseDto<>(reservationMapper.reservationsToReservationResponseDtos(dailyReservations, counselorNames)), HttpStatus.OK
            );
        } else if (month != null) { // month 파라미터를 넣었으면 특정월 조회
            Map<LocalDate, Boolean> monthlyReservations = reservationService.getMonthlyReservations(counselor, month);
            return new ResponseEntity<>(
                    new SingleResponseDto<>(monthlyReservations), HttpStatus.OK
            );
        }
        // 쿼리 파라미터를 아무것도 안 넣었을 때
        throw new BusinessLogicException(ExceptionCode.PARAM_NOT_FOUND);
    }

    // 상담사 프로필 수정
    @PatchMapping
    public ResponseEntity<?> patchCounselor(Authentication authentication,
                                            @RequestBody CounselorDto.Patch patchDto){
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;
        LoginDto.UserType userType = auth.getUserType();
        // Counselor만 이용 가능한 요청
        if(!userType.equals(LoginDto.UserType.COUNSELOR)) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        Counselor counselor = counselorMapper.counselorPatchDtoToCounselor(patchDto);
        long counselorId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"));
        counselor.setCounselorId(counselorId);

        // 서비스 로직 실행
        Counselor patchedCounselor = counselorService.updateCounselor(counselor);

        return new ResponseEntity<>(
                new SingleResponseDto<>(counselorMapper.counselorToCounselorResponseDto(patchedCounselor)), HttpStatus.OK
        );
    }

    // 기본 상담 시간 등록
    @PostMapping("/default-days")
    public ResponseEntity<?> postDefaultDays(Authentication authentication,
                                             @RequestBody CounselorDto.DefaultDays postDto){
        long counselorId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"));

        // 기본 상담 시간 등록
        counselorService.setDefaultDays(counselorId, postDto, true);
        // 실제 AvailableTimes 등록
        counselorService.addInitialAvailableTimes(counselorId, 2);

        return new ResponseEntity<>(
                new SingleResponseDto<>(null), HttpStatus.CREATED
        );
    }

    // 기본 상담 시간 변경
    @PatchMapping("/default-days")
    public ResponseEntity<?> patchDefaultDays(Authentication authentication,
                                              @RequestBody CounselorDto.DefaultDays patchDto){
        long counselorId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"));

        counselorService.setDefaultDays(counselorId, patchDto, false);

        return new ResponseEntity<>(
                new SingleResponseDto<>(null), HttpStatus.OK
        );
    }

    // 자신의 기본 상담 시간 조회
    @GetMapping("/default-days")
    public ResponseEntity<?> getDefaultDay(Authentication authentication,
                                           @RequestParam DayOfWeek dayOfWeek){
        long counselorId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"));
        List<LocalTime> times = counselorService.getDefaultTimesOfDay(counselorId, dayOfWeek);

        return new ResponseEntity<>(
                new SingleResponseDto<>(counselorMapper.defaultTimesToFormattedDefaultTimes(times)), HttpStatus.OK
        );
    }

    // 단일 상담사 조회
    @GetMapping("/{counselorId}")
    public ResponseEntity<?> getCounselor(@PathVariable @Positive long counselorId
                                          /*,Authentication authentication*/){
        Counselor findCounselor = counselorService.findCounselor(counselorId);
        return new ResponseEntity<>(
                new SingleResponseDto<>(counselorMapper.counselorToCounselorResponseDto(findCounselor)), HttpStatus.OK
        );
    }
    // 활동중인 전체 상담사 조회
    @GetMapping
    public ResponseEntity<?> getCounselors(/*Authentication authentication*/){
        List<Counselor> activeCounselors = counselorService.getAllActiveCounselors();
        return new ResponseEntity<>(
                new SingleResponseDto<>(counselorMapper.counselorsToCounselorResponseDtos(activeCounselors)), HttpStatus.OK
        );
    }

    // 자신의 특정일 상담 슬롯 조회
    @GetMapping("/available-dates")
    public ResponseEntity<?> getAvailableDate(Authentication authentication,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        long counselorId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"));
        AvailableDate availableDate = counselorService.getAvailableDate(counselorId, date);
        return new ResponseEntity<>(
                new SingleResponseDto<>(counselorMapper.availableDateToAvailableDateDto(availableDate)), HttpStatus.OK
        );
    }

    // 자신의 특정일 상담 슬롯 변경
    @PatchMapping("/available-dates")
    public ResponseEntity<?> patchAvailableDate(Authentication authentication,
                                                @RequestBody AvailableDateDto.Patch patchDto){
        long counselorId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"));
        counselorService.updateAvailableDate(counselorId, patchDto);

        return new ResponseEntity<>(
                new SingleResponseDto<>(null), HttpStatus.OK
        );
    }
}
