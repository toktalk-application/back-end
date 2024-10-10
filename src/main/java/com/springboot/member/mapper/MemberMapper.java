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

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ReservationMapper.class)
public interface MemberMapper {
    Member memberPostDtoToMember(MemberDto.Post postDto);

    Member memberPatchDtoToMember(MemberDto.Patch patchDto);

    @Mapping(source = "reservations", target = "reservations")
    MemberDto.Response memberToMemberResponseDto(Member member);

    DailyMood dailyMoodPostDtoToDailyMood(DailyMoodDto.Post postDto);

    Map<LocalDate, DailyMoodDto.Response> dailyMoodMapToDailyMoodResponseDtoMap(Map<LocalDate, DailyMood> dailyMoodMap);
}
