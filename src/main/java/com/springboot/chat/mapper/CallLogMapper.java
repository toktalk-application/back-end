package com.springboot.chat.mapper;

import com.springboot.chat.dto.CallLogDto;
import com.springboot.chat.entity.CallLog;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CallLogMapper {
    CallLogDto.Response callLogToCallLogResponseDto(CallLog callLog);

    List<CallLogDto.Response> callLogsToCallLogResponseDtos(List<CallLog> callLogs);

}
