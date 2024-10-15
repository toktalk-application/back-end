package com.springboot.reservation.repository;

import com.springboot.counselor.entity.Counselor;
import com.springboot.member.entity.Member;
import com.springboot.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByMember(Member member);
    List<Reservation> findByCounselorId(long counselorId);

    List<Reservation> findByReservationStatus(Reservation.ReservationStatus reservationStatus);
}
