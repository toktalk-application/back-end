package com.springboot.reservation.controller;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.firebase.service.NotificationService;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.reservation.dto.ReportDto;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.dto.ReviewDto;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservations")
@AllArgsConstructor
public class ReservationController {
    private final String DEFAULT_URL = "/reservations";
    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;
    private final MemberService memberService;
    private final CounselorService counselorService;
    private final NotificationService notificationService;

    // 상담 예약 등록
    @PostMapping
    public ResponseEntity<?> postReservation(Authentication authentication,
            @RequestBody ReservationDto.Post postDto){
        Reservation tempReservation = reservationMapper.reservationPostDtoToReservation(postDto);

        // Member가 아니면 예약 불가
        if(CredentialUtil.getUserType(authentication) != LoginDto.UserType.MEMBER) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        // 멤버 찾아서 넣기
        long memberId = Long.parseLong(CredentialUtil.getCredentialField(authentication,"memberId"));
        Member member = memberService.findMember(memberId);
        tempReservation.setMember(member);

        // 상담사 있는지 검사(없으면 예외 발생)
        Counselor counselor = counselorService.findCounselor(postDto.getCounselorId());
        tempReservation.setCounselorName(counselor.getName());

        // 상담사는 자격 인증이 완료되어 있고 활동 상태여야 함
        if(counselor.getCounselorStatus() != Counselor.Status.ACTIVE) throw new BusinessLogicException(ExceptionCode.INVALID_COUNSELOR);

        // 서비스 로직 실행
        Reservation reservation = reservationService.createReservation(tempReservation, postDto.getDate(), postDto.getStartTimes());
        boolean notificationSent = notificationService.sendReservationNotification(reservation.getReservationId());
        URI location = UriCreator.createUri(DEFAULT_URL, reservation.getReservationId());
        return ResponseEntity.created(location)
                .header("Notification-Sent", String.valueOf(notificationSent))
                .build();
    }

    // 단일 상담 조회
    @GetMapping("/{reservationId}")
    public ResponseEntity<?> getReservation(/*Authentication authentication,*/
                                            @PathVariable long reservationId){
        Reservation reservation = reservationService.findReservation(reservationId);

        Counselor counselor = counselorService.findCounselor(reservation.getCounselorId());

        return new ResponseEntity<>(
                new SingleResponseDto<>(reservationMapper.reservationToReservationResponseDto(reservation)), HttpStatus.OK
        );
    }

    // 멤버가 자신이 예약한 특정일 상담 목록 조회
    @GetMapping("/daily")
    public ResponseEntity<?> getMyReservations(Authentication authentication,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                               @RequestParam(required = false) boolean exceptCancelledReservation){
        // 회원만 요청 가능
        LoginDto.UserType userType = CredentialUtil.getUserType(authentication);
        if(!userType.equals(LoginDto.UserType.MEMBER)) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        long memberId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"));

        List<Reservation> reservations = reservationService.getDailyReservationsByMember(memberId, date, exceptCancelledReservation);

        return new ResponseEntity<>(
                new SingleResponseDto<>(reservationMapper.reservationsToReservationResponseDtos(reservations)), HttpStatus.OK
        );
    }

    // 회원이 특정월에 대한, 날짜별로 자신이 예약한 상담이 있는지 여부 조회
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReservations(Authentication authentication,
                                                    @RequestParam @DateTimeFormat(pattern = "yyyy-mm") YearMonth month){
        long memberId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"));

        Map<LocalDate, Boolean> monthlyReservations = reservationService.getMonthlyReservationsByMember(memberId, month);
        return new ResponseEntity<>(
                new SingleResponseDto<>(monthlyReservations), HttpStatus.OK
        );
    }

    // 회원이 특정월에 대해 자신이 예약한 모든 상담 조회
    @GetMapping("/monthly-detail")
    public ResponseEntity<?> getDetailedMonthlyReservations(Authentication authentication,
                                                    @RequestParam @DateTimeFormat(pattern = "yyyy-mm") YearMonth month){
        long memberId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"));

        List<Reservation> reservations = reservationService.getDetailedMonthlyReservations(memberId, month);

        return new ResponseEntity<>(
                new SingleResponseDto<>(reservationMapper.reservationsToReservationResponseDtos(reservations)), HttpStatus.OK
        );
    }

    // 특정 상담사에 대한 특정일 또는 월별 예약 정보 조회
    @GetMapping
    public ResponseEntity<?> getReservations(Authentication authentication,
            @RequestParam @Positive long counselorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-mm") YearMonth month){

        if(date != null){ // date 파라미터를 넣었으면 특정일 조회
            List<Reservation> dailyReservations = reservationService.getDailyReservationsWithCounselor(counselorId, date);

            return new ResponseEntity<>(
                    new SingleResponseDto<>(reservationMapper.reservationsToReservationResponseDtos(dailyReservations)), HttpStatus.OK
            );
        } else if (month != null) { // month 파라미터를 넣었으면 특정월 조회
            Map<LocalDate, Boolean> monthlyReservations = reservationService.getMonthlyReservationsWithCounselor(counselorId, month);
            return new ResponseEntity<>(
                    new SingleResponseDto<>(monthlyReservations), HttpStatus.OK
            );
        }
        // 쿼리 파라미터를 아무것도 안 넣었을 때
        throw new BusinessLogicException(ExceptionCode.PARAM_NOT_FOUND);
    }

    // 특정 상담사에 대한 월별 예약 세부 정보 조회
    @GetMapping("/{counselorId}/monthly-detail")
    public ResponseEntity<?> getMonthlyDetailReservations(Authentication authentication,
                                                          @PathVariable @Positive long counselorId,
                                                          @RequestParam @DateTimeFormat(pattern = "yyyy-mm") YearMonth month){

        List<Reservation> reservations = reservationService.getMonthlyDetailReservations(counselorId, month);

        return new ResponseEntity<>(
                new SingleResponseDto<>(reservationMapper.reservationsToReservationResponseDtos(reservations)), HttpStatus.OK
        );
    }

    // 리뷰 등록
    @PostMapping("/{reservationId}/reviews")
    public ResponseEntity<?> postReview(@PathVariable long reservationId,
                                        @RequestBody ReviewDto.Post reviewDto,
                                        Authentication authentication){
        reservationService.registerReview(reservationId, reservationMapper.reviewPostDToToReview(reviewDto), authentication);

        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    // 상담사 진단 등록
    @PostMapping("/{reservationId}/reports")
    public ResponseEntity<?> postReport(@PathVariable long reservationId,
                                        @RequestBody ReportDto.Post reportDto,
                                        Authentication authentication
                                        ){
        reservationService.registerReport(reservationId, reservationMapper.reportPostDtoToReport(reportDto), authentication);
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    // 예약 취소 (회원, 상담사 모두 가능)
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable long reservationId,
                                            @RequestParam(required = false) String cancelReason,
                                            Authentication authentication){
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;

        switch (auth.getUserType()){
            case MEMBER:
                reservationService.cancelReservationByMember(reservationId);
                break;
            case COUNSELOR:
                reservationService.cancelReservationByCounselor(reservationId, cancelReason);
                break;
            default:
                throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);
        }
        return ResponseEntity.noContent().build();
    }
}
