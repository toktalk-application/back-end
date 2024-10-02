package com.springboot.reservation.service;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.entity.Review;
import com.springboot.reservation.repository.ReservationRepository;
import com.springboot.utils.CredentialUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final CounselorService counselorService;
    public Reservation createReservation(Reservation reservation, LocalDate date, List<LocalTime> startTimes){
        Counselor counselor = counselorService.findCounselor(reservation.getCounselorId());

        // 예약 불가능한 날짜
        if(!counselor.getAvailableDates().containsKey(date)) throw new BusinessLogicException(ExceptionCode.UNAVAILABLE_DATE);
        // 예약 가능한 시간인지 검사 (예외는 내부적으로 처리, 예약 시간 등록도 내부적으로 처리)
        counselor.getAvailableDate(date).validateReservationTimes(reservation, startTimes);
        // 예약 시간들이 연속적인지 검사
        Collections.sort(startTimes);
        int firstHour = startTimes.get(0).getHour();
        for(int i = 1; i< startTimes.size(); i++){
            // 첫 예약 타임이 9시라면 그 다음 타임은 10시, 다다음은 11시여야 함. 아니라면 예외 반환
            if(startTimes.get(i).getHour() != firstHour + i) throw new BusinessLogicException(ExceptionCode.DISCONTINUOUS_TIME);
        }
        return reservationRepository.save(reservation);
    }
    // 리뷰 등록
    public void registerReview(long reservationId, ReservationDto.Review reviewDto, Authentication authentication){
        // Member만 리뷰 작성 가능
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;
        if(auth.getUserType() != LoginDto.UserType.MEMBER) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        // 진짜로 상담 받은 회원인지 검사
        Reservation reservation = findReservation(reservationId);
        long memberId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"));

        // 다른 놈인데?
        if(reservation.getMember().getMemberId() != memberId) throw new BusinessLogicException(ExceptionCode.UNMATCHED_MEMBER);

        // 상담이 끝난 상태여야만 리뷰 작성 가능
        if(reservation.getReservationStatus() != Reservation.ReservationStatus.COMPLETED) throw new BusinessLogicException(ExceptionCode.UNCOMPLETE_COUNSELING);

        // 이미 리뷰 있어도 안됨
        if(reservation.getReview() != null) throw new BusinessLogicException(ExceptionCode.REVIEW_EXIST);

        // 문제 없으면 리뷰 등록
        Review review = new Review();
        review.setContent(reviewDto.getContent());
        review.setRating(reviewDto.getRating());
        reservation.setReview(review);
        reservationRepository.save(reservation);
    }

    public Reservation findReservation(long reservationId){
        return findVerifiedReservation(reservationId);
    }

    private Reservation findVerifiedReservation(long reservationId){
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservationId);
        return optionalReservation.orElseThrow(() -> new BusinessLogicException(ExceptionCode.RESERVATION_NOT_FOUND));
    }

    public void cancelReservationByMember(long reservationId){
        // 예약 정보 찾기
        Reservation reservation = findReservation(reservationId);
        // 상담사 밑으로 잡혀있는 예약 정보 없애기
        reservation.getReservationTimes().forEach(time -> {
            time.setReservation(null);
        });
        // 예약 상태 바꾸기
        reservation.setReservationStatus(Reservation.ReservationStatus.CANCELLED_BY_CLIENT);

        // 바뀐 상태 저장하고 리턴
        reservationRepository.save(reservation);
    }
    public void cancelReservationByCounselor(long reservationId, int cancelReason){
        // 예약 정보 찾기
        Reservation reservation = findReservation(reservationId);
        // 상담사 밑으로 잡혀있는 예약 정보 없애기
        reservation.getReservationTimes().forEach(time -> {
            time.setReservation(null);
        });
        // 예약 상태 바꾸기
        reservation.setReservationStatus(Reservation.ReservationStatus.CANCELLED_BY_COUNSELOR);
        // 취소 사유 등록
        switch (cancelReason){
            case 1 -> reservation.setCancelComment("ㅈㅅ");
            case 2 -> reservation.setCancelComment("ㅈㅅ ㅋ");
            case 3 -> reservation.setCancelComment("ㅈㅅ ㅋㅋ");
            case 4 -> reservation.setCancelComment("ㅈㅅ ㅋㅋㅋ");
            case 5 -> reservation.setCancelComment("ㅈㅅ ㅋㅋㅋㅋ");
            default -> throw new BusinessLogicException(ExceptionCode.INVALID_CANCLE_REASON);
        }
        // 바뀐 상태 저장하고 리턴
        reservationRepository.save(reservation);
    }
}
