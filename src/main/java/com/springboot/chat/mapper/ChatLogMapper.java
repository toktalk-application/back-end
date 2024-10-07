package com.springboot.chat.mapper;

import com.springboot.chat.dto.ChatLogDto;
import com.springboot.chat.entity.ChatLog;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatLogMapper {
    ChatLog chatLogPostDtoToChatLog(ChatLogDto.Post chatMessage);
    ChatLogDto.Response chatLogToChatLogResponseDto(ChatLog chatLog);
    List<ChatLogDto.Response> chatLogsToChatLogResponseDtos(List<ChatLog> chatLogs);
}
