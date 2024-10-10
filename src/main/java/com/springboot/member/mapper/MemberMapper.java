package com.springboot.member.mapper;

import com.springboot.member.dto.DailyMoodDto;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.DailyMood;
import com.springboot.member.entity.Member;
import com.springboot.reservation.mapper.ReservationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ReservationMapper.class)
public interface MemberMapper {
    Member memberPostDtoToMember(MemberDto.Post postDto);

    Member memberPatchDtoToMember(MemberDto.Patch patchDto);

    @Mapping(source = "reservations", target = "reservations")
    MemberDto.Response memberToMemberResponseDto(Member member);

    DailyMood dailyMoodPostDtoToDailyMood(DailyMoodDto.Post postDto);

    default Map<LocalDate, DailyMood.Mood> dailyMoodMapToDailyMoodResponseDtoMap(Map<LocalDate, DailyMood> dailyMoodMap){
        return dailyMoodMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getMood()
                        ));
    };
}
