package com.springboot.counselor.controller;

import com.springboot.counselor.dto.CounselorDto;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.mapper.CounselorMapper;
import com.springboot.counselor.service.CounselorService;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.mapper.ReservationMapper;
import com.springboot.response.MultiResponseDto;
import com.springboot.response.SingleResponseEntity;
import com.springboot.utils.UriCreator;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/counselors")
@AllArgsConstructor
public class CounselorController {
    private final String DEFAULT_URL = "/counselors";
    private final CounselorMapper counselorMapper;
    private final CounselorService counselorService;
    private final ReservationMapper reservationMapper;
    @PostMapping
    public ResponseEntity<Counselor> postCounselor(@RequestBody CounselorDto.Post postDto){
        Counselor tempCounselor = counselorMapper.counselorPostDtoToCounselor(postDto);
        Counselor savedCounselor = counselorService.createCounselor(tempCounselor);

        URI location = UriCreator.createUri(DEFAULT_URL, savedCounselor.getCounselorId());
        return ResponseEntity.created(location).build();
    }
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDto.Response>> getReservations(Authentication authentication,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate searchDate){

        Counselor counselor = counselorService.findCounselor(1);
        Set<Reservation> reservations = new HashSet<>();
        counselor.getAvailableDate(searchDate).getAvailableTimes().forEach(time -> {
            if(time.getReservation() != null){
                reservations.add(time.getReservation());
            }
        });
        List<ReservationDto.Response> result = reservationMapper.reservationsToReservationResponseDtos(reservations.stream().toList());
        return new SingleResponseEntity<>(result, HttpStatus.OK);
    }
}
