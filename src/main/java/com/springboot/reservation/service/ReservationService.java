package com.springboot.reservation.service;

import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
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


        return reservationRepository.save(reservation);
    }

    public Reservation findReservation(long reservationId){
        return findVerifiedReservation(reservationId);
    }

    private Reservation findVerifiedReservation(long reservationId){
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservationId);
        return optionalReservation.orElseThrow(() -> new BusinessLogicException(ExceptionCode.RESERVATION_NOT_FOUND));
    }
}
