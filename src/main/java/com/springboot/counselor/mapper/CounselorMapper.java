package com.springboot.counselor.mapper;

import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.counselor.dto.AvailableDateDto;
import com.springboot.counselor.dto.AvailableTimeDto;
import com.springboot.counselor.dto.CounselorDto;
import com.springboot.counselor.entity.Counselor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = AvailableDateMapper.class)
public interface CounselorMapper {
    Counselor counselorPostDtoToCounselor(CounselorDto.Post postDto);
    Counselor counselorPatchDtoToCounselor(CounselorDto.Patch patchDto);
    default CounselorDto.Response counselorToCounselorResponseDto(Counselor counselor){
        List<AvailableDate> availableDateList = new ArrayList<>(counselor.getAvailableDates().values());

        return new CounselorDto.Response(
                counselor.getCounselorId(),
                counselor.getBirth(),
                counselor.getGender(),
                counselor.getCounselorStatus(),
                counselor.getName(),
                counselor.getUserId(),
                counselor.getCompany(),
                availableDateList.stream()
                        .map(date -> new AvailableDateDto(
                                date.getAvailableDateId(),
                                date.getDate(),
                                date.getAvailableTimes().stream()
                                        .map(time -> new AvailableTimeDto(
                                                time.getAvailableTimeId(),
                                                time.getStartTime(),
                                                time.getEndTime(),
                                                time.getReservation() != null
                                        )).collect(Collectors.toList())
                        ))
                        .collect(Collectors.toList()),
                counselor.getChatPrice(),
                counselor.getCallPrice(),
                counselor.getCreatedAt(),
                counselor.getModifiedAt()
        );
    };
    /*@Mapping(source = "availableDates", target = "availableDates")
    CounselorDto.Response counselorToCounselorResponseDto(Counselor counselor);*/
}
