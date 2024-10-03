package com.springboot.counselor.controller;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.counselor.dto.CounselorDto;
import com.springboot.counselor.dto.LicenseDto;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.mapper.CounselorMapper;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.mapper.ReservationMapper;
import com.springboot.reservation.service.ReservationService;
import com.springboot.response.MultiResponseDto;
import com.springboot.response.SingleResponseDto;
import com.springboot.response.SingleResponseEntity;
import com.springboot.utils.CredentialUtil;
import com.springboot.utils.UriCreator;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping("/counselors")
@AllArgsConstructor
public class CounselorController {
    private final String DEFAULT_URL = "/counselors";
    private final CounselorMapper counselorMapper;
    private final CounselorService counselorService;
    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    // 상담사 회원 가입
    @PostMapping
    public ResponseEntity<?> postCounselor(@RequestBody CounselorDto.Post postDto){
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

    // 특정 상담사에 대한 특정일 또는 월별 예약 목록 조회
    @GetMapping("/{counselorId}/reservations")
    public ResponseEntity<?> getReservations(/*Authentication authentication,*/
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-mm") YearMonth month,
                                                                         @PathVariable long counselorId){
        // 상담사 찾아오기
        Counselor counselor = counselorService.findCounselor(counselorId);

        if(date != null){
            List<Reservation> dailyReservations = reservationService.getDailyReservations(counselor, date);
            return new ResponseEntity<>(
                    new SingleResponseDto<>(reservationMapper.reservationsToReservationResponseDtos(dailyReservations)), HttpStatus.OK
            );
        } else if (month != null) {
            Map<LocalDate, Boolean> monthlyReservations = reservationService.getMonthlyReservations(counselor, month);
            return new ResponseEntity<>(
                    new SingleResponseDto<>(monthlyReservations), HttpStatus.OK
            );
        }
        // 쿼리 파라미터를 아무것도 안 넣었을 때
        throw new BusinessLogicException(ExceptionCode.PARAM_NOT_FOUND);
    }


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

    @GetMapping("/{counselorId}")
    public ResponseEntity<?> getCounselor(@PathVariable long counselorId
                                          /*,Authentication authentication*/){
        Counselor findCounselor = counselorService.findCounselor(counselorId);
        return new ResponseEntity<>(
                new SingleResponseDto<>(counselorMapper.counselorToCounselorResponseDto(findCounselor)), HttpStatus.OK
        );
    }
}
