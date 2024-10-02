package com.springboot.reservation.controller;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.mapper.ReservationMapper;
import com.springboot.reservation.repository.ReservationRepository;
import com.springboot.reservation.service.ReservationService;
import com.springboot.utils.UriCreator;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/reservations")
@AllArgsConstructor
public class ReservationController {
    private final String DEFAULT_URL = "/reservations";
    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;
    private final ReservationRepository reservationRepository;
    private final MemberService memberService;
    private final CounselorService counselorService;
    @PostMapping
    public ResponseEntity<Reservation> postReservation(@RequestBody ReservationDto.Post postDto){
        Reservation tempReservation = reservationMapper.reservationPostDtoToReservation(postDto);

        // 멤버 찾아서 넣기
        Member member = memberService.findMember(postDto.getMemberId());
        tempReservation.setMember(member);

        // 상담사는 있는지만 검사(없으면 예외 발생)
        counselorService.findCounselor(postDto.getCounselorId());

        Reservation reservation = reservationService.createReservation(tempReservation, postDto.getDate(), postDto.getStartTimes());

        URI location = UriCreator.createUri(DEFAULT_URL, reservation.getReservationId());
        return ResponseEntity.created(location).build();
    }

    /*@GetMapping
    public ResponseEntity<Reservation> getReservation(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime){
        if(startTime != null){
            Reservation reservation = reservationService.getReservation(startTime);
            return new ResponseEntity<>(reservation, HttpStatus.OK);
        }
        throw new BusinessLogicException(ExceptionCode.PARAM_NOT_FOUND);
    }*/

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@PathVariable long reservationId,
                                            Authentication authentication){
        // 예약 정보 찾기
        Reservation reservation = reservationService.findReservation(reservationId);
        // 상담사 쪽에 잡혀있는 예약 정보 없애기
        reservation.getReservationTimes().forEach(time -> {
            time.setReservation(null);
        });
        // 예약 상태 바꾸기
        LoginDto.UserType userType = ((CustomAuthenticationToken) authentication).getUserType();

        Reservation.ReservationStatus newStatus;

        switch (userType){
            case MEMBER -> newStatus = Reservation.ReservationStatus.CANCELLED_BY_CLIENT;
            case COUNSELOR -> newStatus = Reservation.ReservationStatus.CANCELLED_BY_COUNSELOR;
            default -> throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);
        }
        reservation.setReservationStatus(newStatus);

        // 바뀐 상태 저장하고 리턴
        reservationRepository.save(reservation);
        return ResponseEntity.noContent().build();
    }
}
