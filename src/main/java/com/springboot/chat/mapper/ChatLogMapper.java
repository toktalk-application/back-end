package com.springboot.chat.mapper;

import com.springboot.chat.dto.ChatLogDto;
import com.springboot.chat.entity.ChatLog;
import org.mapstruct.Mapper;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ChatLogMapper {
    ChatLog chatLogPostDtoToChatLog(ChatLogDto.Post chatMessage);
    ChatLogDto.Response chatLogToChatLogResponseDto(ChatLog chatLog);
    List<ChatLogDto.Response> chatLogsToChatLogResponseDtos(List<ChatLog> chatLogs);
    default ChatLogDto.ChatResponse chatLogToChatLogChatResponseDto(ChatLog chatLog) {
        ChatLogDto.ChatResponse.ChatResponseBuilder response = ChatLogDto.ChatResponse.builder();
        response.logId(chatLog.getLogId());
        response.sender(chatLog.getSender());
        response.message(chatLog.getMessage());
        response.timeOnly(chatLog.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")));

        return response.build();
    }
    default List<ChatLogDto.ChatResponse> chatLogsToChatLogChatResponseDtos(List<ChatLog> chatLogs) {
        return chatLogs.stream()
                .distinct()
                .map(chatLog -> chatLogToChatLogChatResponseDto(chatLog))
                .collect(Collectors.toList());
    }
}
