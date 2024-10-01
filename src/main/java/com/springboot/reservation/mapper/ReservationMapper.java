package com.springboot.reservation.mapper;

import com.springboot.counselor.available_date.AvailableTime;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {
    Reservation reservationPostDtoToReservation(ReservationDto.Post postDto);
    default ReservationDto.Response reservationToReservationResponseDto(Reservation reservation){
        LocalTime startTime = LocalTime.MAX;
        LocalTime endTime = LocalTime.MIN;

        // startTime중 가장 이른 시점과 endTime중 가장 나중 시점을 뽑기
        for(AvailableTime time : reservation.getReservationTimes()){
            startTime = startTime.isBefore(time.getStartTime()) ? startTime : time.getStartTime();
            endTime = endTime.isAfter(time.getEndTime()) ? endTime : time.getEndTime();
        }

        return new ReservationDto.Response(
                reservation.getReservationId(),
                reservation.getCounselorId(),
                reservation.getComment(),
                reservation.getType(),
                reservation.getReservationTimes().get(0).getAvailableDate().getDate(),
                startTime,
                endTime
        );
    };
    default List<ReservationDto.Response> reservationsToReservationResponseDtos(List<Reservation> reservations){
        List<ReservationDto.Response> response = new ArrayList<>();
        for (Reservation reservation : reservations) {
            response.add(reservationToReservationResponseDto(reservation));
        }
        return response;
    }
}
