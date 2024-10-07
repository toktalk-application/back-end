package com.springboot.chat.mapper;

import com.springboot.chat.dto.ChatRoomDto;
import com.springboot.chat.entity.ChatRoom;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = ChatLogMapper.class)
public interface ChatRoomMapper {
    default ChatRoomDto.Response chatRoomToChatRoomResponseDto(ChatRoom chatRoom, ChatLogMapper chatLogMapper) {
        ChatRoomDto.Response.ResponseBuilder response = ChatRoomDto.Response.builder();
            response.roomId(chatRoom.getRoomId());
            response.memberId(chatRoom.getMember().getMemberId());
            response.counselorId(chatRoom.getCounselor().getCounselorId());
            response.roomStatus(chatRoom.getRoomStatus().getStatus());
            response.createdAt(chatRoom.getCreatedAt());
            response.chatLogs(chatLogMapper.chatLogsToChatLogResponseDtos(chatRoom.getChatLogs()));

            return response.build();
    }
}
