package com.springboot.reservation.controller;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.entity.Review;
import com.springboot.reservation.mapper.ReservationMapper;
import com.springboot.reservation.repository.ReservationRepository;
import com.springboot.reservation.service.ReservationService;
import com.springboot.utils.CredentialUtil;
import com.springboot.utils.UriCreator;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    // 상담 예약 등록
    @PostMapping
    public ResponseEntity<?> postReservation(@RequestBody ReservationDto.Post postDto){
        Reservation tempReservation = reservationMapper.reservationPostDtoToReservation(postDto);

        // 멤버 찾아서 넣기
        Member member = memberService.findMember(postDto.getMemberId());
        tempReservation.setMember(member);

        // 상담사 있는지 검사(없으면 예외 발생)
        Counselor counselor = counselorService.findCounselor(postDto.getCounselorId());

        // 상담사는 자격 인증이 완료되어 있고 활동 상태여야 함
        if(counselor.getCounselorStatus() != Counselor.CounselorStatus.ACTIVE) throw new BusinessLogicException(ExceptionCode.INVALID_COUNSELOR);

        Reservation reservation = reservationService.createReservation(tempReservation, postDto.getDate(), postDto.getStartTimes());

        URI location = UriCreator.createUri(DEFAULT_URL, reservation.getReservationId());
        return ResponseEntity.created(location).build();
    }

    // 리뷰 등록
    @PostMapping("/{reservationId}/reviews")
    public ResponseEntity<?> postReview(@PathVariable long reservationId,
                                        @RequestBody ReservationDto.Review reviewDto,
                                        Authentication authentication){
        reservationService.registerReview(reservationId, reviewDto, authentication);

        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    // 상담사 진단 등록
    @PostMapping("/{reservationId}/reports")
    public ResponseEntity<?> postReport(@PathVariable long reservationId,
                                        @RequestBody ReservationDto.Report reportDto,
                                        Authentication authentication
                                        ){
        reservationService.registerReport(reservationId, reportDto, authentication);
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@PathVariable long reservationId,
                                            @RequestParam(required = false) int cancelReason,
                                            Authentication authentication){

        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;

        switch (auth.getUserType()){
            case MEMBER -> reservationService.cancelReservationByMember(reservationId);
            case COUNSELOR -> reservationService.cancelReservationByCounselor(reservationId, cancelReason);
            default -> throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);
        }
        return ResponseEntity.noContent().build();
    }
}
