package com.springboot.counselor.mapper;

import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.counselor.dto.AvailableDateDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AvailableDateMapper {
    List<AvailableDateDto> availableDatesToAvailableDateDtos(List<AvailableDate> availableDates);
}
