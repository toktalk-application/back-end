package com.springboot.reservation.mapper;

import com.springboot.counselor.available_date.AvailableTime;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.counselor.service.CounselorService;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.entity.Report;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {
    Reservation reservationPostDtoToReservation(ReservationDto.Post postDto);
    default ReservationDto.Response reservationToReservationResponseDto(Reservation reservation){
        // 시작시간, 끝시간 구하기
        Reservation.TimePeriod timePeriod = reservation.getReservationTimePeriod();
        LocalTime startTime = timePeriod.getStartTime();
        LocalTime endTime = timePeriod.getEndTime();

        // 예약 취소된 상태면 reservationTimes가 비어 있기 때문에 예외 처리
        LocalDate date = reservation.getReservationTimes().isEmpty() ? null : reservation.getReservationTimes().get(0).getAvailableDate().getDate();

        // 리뷰 dto로 변환
        ReservationDto.Review reviewDto = null;
        if(reservation.getReview() != null){
            Review review = reservation.getReview();
            reviewDto = new ReservationDto.Review(review.getContent(), review.getRating(), review.getCreatedAt());
        }
        // 진단 dto로 변환
        ReservationDto.Report reportDto = null;
        if(reservation.getReport() != null){
            Report report = reservation.getReport();
            reportDto = new ReservationDto.Report(report.getContent(), report.getCreatedAt());
        }

        Optional<Integer> depressionScoreOptional = Optional.ofNullable(reservation.getMember().getDepressionScore());
        int depressionScore = depressionScoreOptional.orElse(-1);

        return new ReservationDto.Response(
                reservation.getReservationId(),
                reservation.getCounselorId(),
                reservation.getMember().getNickname(),
                reservation.getMember().getBirth().getYear(),
                reservation.getMember().getGender(),
                depressionScore == -1 ? "없음" : String.valueOf(depressionScore),
                reservation.getCounselorName(),
                reservation.getComment(),
                reservation.getType(),
                reservation.getReservationStatus(),
                date,
                startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                reviewDto,
                reportDto
        );
    };
    default List<ReservationDto.Response> reservationsToReservationResponseDtos(List<Reservation> reservations){
        List<ReservationDto.Response> response = new ArrayList<>();
        /*for (Reservation reservation : reservations) {
            response.add(reservationToReservationResponseDto(reservation, counselorNames));
        }
        return response;*/
        for(int i = 0; i< reservations.size(); i++){
            response.add(reservationToReservationResponseDto(reservations.get(i)));
        }
        return response;
    }
}
