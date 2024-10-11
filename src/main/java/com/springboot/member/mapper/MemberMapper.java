package com.springboot.member.mapper;

import com.springboot.member.dto.DailyMoodDto;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.DailyMood;
import com.springboot.member.entity.Member;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.mapper.ReservationMapper;
import com.springboot.testresult.entity.TestResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE/*, uses = ReservationMapper.class*/)
public interface MemberMapper {

    ReservationMapper reservationMapper = Mappers.getMapper(ReservationMapper.class);
    Member memberPostDtoToMember(MemberDto.Post postDto);

    Member memberPatchDtoToMember(MemberDto.Patch patchDto);

    /*@Mapping(source = "reservations", target = "reservations")*/
    default MemberDto.Response memberToMemberResponseDto(Member member){
        // 우울증 테스트 정보
        List<TestResult> testResults = member.getTestResults();
        TestResult lastTestResult = testResults.isEmpty() ? null : testResults.get(testResults.size() - 1);
        // 예약 정보(맵퍼 주입)
        List<ReservationDto.Response> reservationDtos = reservationMapper.reservationsToReservationResponseDtos(member.getReservations());
        return new MemberDto.Response(
                member.getMemberId(),
                member.getUserId(),
                member.getNickname(),
                member.getBirth(),
                member.getGender(),
                lastTestResult,
                reservationDtos
        );
    };

    DailyMood dailyMoodPostDtoToDailyMood(DailyMoodDto.Post postDto);

    default Map<LocalDate, DailyMood.Mood> dailyMoodMapToDailyMoodResponseDtoMap(Map<LocalDate, DailyMood> dailyMoodMap){
        return dailyMoodMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getMood()
                        ));
    };
}
