package com.springboot.counselor.mapper;

import com.springboot.counselor.dto.CounselorDto;
import com.springboot.counselor.entity.Counselor;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CounselorMapper {
    Counselor counselorPostDtoToCounselor(CounselorDto.Post postDto);
    Counselor counselorPatchDtoToCounselor(CounselorDto.Patch patchDto);
    CounselorDto.Response counselorToCounselorResponseDto(Counselor counselor);
}
