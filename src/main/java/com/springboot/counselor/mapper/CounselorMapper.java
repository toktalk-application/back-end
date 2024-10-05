package com.springboot.counselor.mapper;

import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.counselor.available_date.AvailableTime;
import com.springboot.counselor.dto.*;
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

        // 자격증 -> 자격증 dto 변환
        List<LicenseDto.Response> licenseDtos = counselor.getLicenses().stream()
                .map(license -> new LicenseDto.Response(
                        license.getLicenseId(),
                        license.getLicenseName(),
                        license.getOrganization(),
                        license.getIssueDate()
                )).collect(Collectors.toList());
        // 경력 -> 경력 dto 변환
        List<CareerDto.Response> careerDtos = counselor.getCareers().stream()
                .map(career -> new CareerDto.Response(
                        career.getCareerId(),
                        career.getClassification(),
                        career.getCompany(),
                        career.getResponsibility()
                )).collect(Collectors.toList());

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
                                date.getAvailableTimes().entrySet().stream()
                                        .collect(Collectors.toMap(
                                                entry -> entry.getKey(),
                                                entry -> {
                                                    AvailableTime time = entry.getValue();
                                                    return new AvailableTimeDto(
                                                            time.getAvailableTimeId(),
                                                            time.getStartTime(),
                                                            time.getEndTime(),
                                                            time.getReservation() != null
                                                    );
                                                }
                                        ))
                        ))
                        .collect(Collectors.toList()),
                counselor.getChatPrice(),
                counselor.getCallPrice(),
                careerDtos,
                licenseDtos,
                counselor.getCreatedAt(),
                counselor.getModifiedAt()
        );
    };
    /*@Mapping(source = "availableDates", target = "availableDates")
    CounselorDto.Response counselorToCounselorResponseDto(Counselor counselor);*/
}
