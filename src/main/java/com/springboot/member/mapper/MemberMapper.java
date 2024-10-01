package com.springboot.member.mapper;

import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import com.springboot.reservation.mapper.ReservationMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ReservationMapper.class)
public interface MemberMapper {
    Member memberPostDtoToMember(MemberDto.Post postDto);

    @Mapping(source = "reservations", target = "reservations")
    MemberDto.Response memberToMemberResponseDto(Member member);
}
