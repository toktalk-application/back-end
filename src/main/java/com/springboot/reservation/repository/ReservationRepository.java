package com.springboot.reservation.repository;

import com.springboot.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByMember_MemberId(long memberId);

    List<Reservation> findByCounselorId(long counselorId);
}
