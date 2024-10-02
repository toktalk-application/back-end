package com.springboot.counselor.controller;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.counselor.dto.CounselorDto;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.mapper.CounselorMapper;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.mapper.ReservationMapper;
import com.springboot.response.MultiResponseDto;
import com.springboot.response.SingleResponseDto;
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
import java.util.*;

@RestController
@RequestMapping("/counselors")
@AllArgsConstructor
public class CounselorController {
    private final String DEFAULT_URL = "/counselors";
    private final CounselorMapper counselorMapper;
    private final CounselorService counselorService;
    private final ReservationMapper reservationMapper;
    @PostMapping
    public ResponseEntity<?> postCounselor(@RequestBody CounselorDto.Post postDto){
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

    @PatchMapping
    public ResponseEntity<?> patchCounselor(Authentication authentication,
                                            @RequestBody CounselorDto.Patch patchDto){
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;
        LoginDto.UserType userType = auth.getUserType();
        // Counselor만 이용 가능한 요청
        if(!userType.equals(LoginDto.UserType.COUNSELOR)) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        Map<String, String> credentials = (Map<String, String>) (authentication.getCredentials());

        Counselor counselor = counselorMapper.counselorPatchDtoToCounselor(patchDto);
        long counselorId = Long.parseLong(credentials.get("counselorId"));
        counselor.setCounselorId(counselorId);

        // 서비스 로직 실행
        Counselor patchedCounselor = counselorService.updateCounselor(counselor);

        return new ResponseEntity<>(
                new SingleResponseDto<>(counselorMapper.counselorToCounselorResponseDto(patchedCounselor)), HttpStatus.OK
        );
    }

    @GetMapping("/{counselorId}")
    public ResponseEntity<?> getCounselor(@PathVariable long counselorId
                                          /*,Authentication authentication*/){
        Counselor findCounselor = counselorService.findCounselor(counselorId);
        return new ResponseEntity<>(
                new SingleResponseDto<>(counselorMapper.counselorToCounselorResponseDto(findCounselor)), HttpStatus.OK
        );
    }
}
