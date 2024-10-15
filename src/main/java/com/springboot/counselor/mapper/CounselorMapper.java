package com.springboot.counselor.mapper;

import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.counselor.available_date.AvailableTime;
import com.springboot.counselor.dto.*;
import com.springboot.counselor.entity.Career;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.entity.License;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
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
                        license.getOrganization()
                )).collect(Collectors.toList());
        // 경력 -> 경력 dto 변환
        List<CareerDto.Response> careerDtos = counselor.getCareers().stream()
                .map(career -> new CareerDto.Response(
                        career.getCareerId(),
                        career.getClassification(),
                        career.getCompany(),
                        career.getResponsibility()
                )).collect(Collectors.toList());

        // 평균 별점 계산
        double rating = (double) counselor.getTotalRating() / counselor.getReviews();
        double formattedRating = Math.round(rating * 10) / 10.0;
        String ratingStr = counselor.getReviews() == 0 ? "별점 없음" : String.valueOf(formattedRating);

        return new CounselorDto.Response(
                counselor.getCounselorId(),
                counselor.getBirth(),
                counselor.getGender(),
                counselor.getCounselorStatus(),
                counselor.getName(),
                counselor.getUserId(),
                counselor.getCompany(),
                availableDateList.stream()
                        .map(date -> new AvailableDateDto.Response(
                                date.getAvailableDateId(),
                                date.getDate(),
                                date.getAvailableTimes().entrySet().stream()
                                        .collect(Collectors.toMap(
                                                entry -> entry.getKey(),
                                                entry -> {
                                                    AvailableTime time = entry.getValue();
                                                    return new AvailableTimeDto(
                                                            time.getAvailableTimeId(),
                                                            time.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                                            time.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
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
                counselor.getIntroduction(),
                counselor.getExpertise(),
                counselor.getSessionDescription(),
                counselor.getProfileImage(),
                ratingStr,
                counselor.getReviews(),
                counselor.getCreatedAt(),
                counselor.getModifiedAt()
        );
    };
    List<CounselorDto.Response> counselorsToCounselorResponseDtos(List<Counselor> counselors);

    /*@Mapping(source = "availableDates", target = "availableDates")
    CounselorDto.Response counselorToCounselorResponseDto(Counselor counselor);*/

    default List<String> defaultTimesToFormattedDefaultTimes(List<LocalTime> defaultTimes){
        return defaultTimes.stream()
                .map(time -> time.format(DateTimeFormatter.ofPattern("HH:mm")))
                .collect(Collectors.toList());
    }

    default AvailableDateDto.Response availableDateToAvailableDateDto(AvailableDate availableDate){
        return new AvailableDateDto.Response(
                availableDate.getAvailableDateId(),
                availableDate.getDate(),
                availableDate.getAvailableTimes().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey(),
                                entry -> {
                                    AvailableTime time = entry.getValue();
                                    return new AvailableTimeDto(
                                            time.getAvailableTimeId(),
                                            time.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                            time.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                            time.getReservation() != null
                                    );
                                }
                        ))
        );
    };

    List<AvailableDateDto.Response> availableDatesToAvailableDateResponseDtos(List<AvailableDate> availableDates);

    List<License> licensePostDtosToLicenses(List<LicenseDto.Post> postDtos);

    List<Career> careerPostDtosToCareers(List<CareerDto.Post> postDtos);
}
