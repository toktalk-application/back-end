package com.springboot.chat.mapper;

import com.springboot.chat.dto.ChatRoomDto;
import com.springboot.chat.entity.ChatRoom;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = ChatLogMapper.class)
public interface ChatRoomMapper {
    default ChatRoomDto.SimpleResponse chatRoomToChatRoomSimpleResponseDto(ChatRoom chatRoom) {
        ChatRoomDto.SimpleResponse.SimpleResponseBuilder response = ChatRoomDto.SimpleResponse.builder();
        response.roomId(chatRoom.getRoomId());
        response.counselorName(chatRoom.getCounselor().getName());
        response.nickname(chatRoom.getMember().getNickname());
        response.profileImage(chatRoom.getCounselor().getProfileImage());
        response.roomStatus(chatRoom.getRoomStatus().getStatus());

        if (!chatRoom.getChatLogs().isEmpty()) {

            response.message(chatRoom.getChatLogs().get(chatRoom.getChatLogs().size() - 1).getMessage());
            response.createdAt(chatRoom.getChatLogs().get(chatRoom.getChatLogs().size() - 1).getCreatedAt());
        } else {
            response.message(null);
            response.createdAt(null);
        }

        return response.build();
    }
    default List<ChatRoomDto.SimpleResponse> chatRoomsToChatRoomSimpleResponseDtos (List<ChatRoom> chatRooms) {
        return chatRooms.stream()
                .distinct()
                .map(chatRoom -> chatRoomToChatRoomSimpleResponseDto(chatRoom))
                .collect(Collectors.toList());
    }
    default ChatRoomDto.DetailResponse chatRoomToChatRoomDetailResponseDto(ChatRoom chatRoom, ChatLogMapper chatLogMapper) {
        ChatRoomDto.DetailResponse.DetailResponseBuilder response = ChatRoomDto.DetailResponse.builder();
        response.roomId(chatRoom.getRoomId());
        response.memberId(chatRoom.getMember().getMemberId());
        response.counselorId(chatRoom.getCounselor().getCounselorId());
        response.roomStatus(chatRoom.getRoomStatus().getStatus());
        response.createdAt(chatRoom.getCreatedAt());
        response.chatLogs(chatLogMapper.chatLogsToChatLogChatResponseDtos(chatRoom.getChatLogs()));

        return response.build();
    }
}
