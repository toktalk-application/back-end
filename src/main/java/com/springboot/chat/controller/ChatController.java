package com.springboot.chat.controller;

import com.springboot.chat.dto.ChatLogDto;
import com.springboot.chat.entity.ChatLog;
import com.springboot.chat.entity.ChatRoom;
import com.springboot.chat.mapper.ChatLogMapper;
import com.springboot.chat.service.ChatLogService;
import com.springboot.chat.service.ChatRoomService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final ChatLogService chatLogService;
    private final ChatRoomService chatRoomService;
    private final ChatLogMapper chatLogMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatLogService chatLogService, ChatRoomService chatRoomService, ChatLogMapper chatLogMapper, SimpMessagingTemplate messagingTemplate) {
        this.chatLogService = chatLogService;
        this.chatRoomService = chatRoomService;
        this.chatLogMapper = chatLogMapper;
        this.messagingTemplate = messagingTemplate;
    }


    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable long roomId, ChatLogDto.Post chatMessage) {
        ChatRoom chatRoom = chatRoomService.findVerifiedChatRoom(roomId);

        if (chatRoom.getRoomStatus() == ChatRoom.RoomStatus.CLOSE) {
            throw new BusinessLogicException(ExceptionCode.CHAT_ROOM_CLOSED);
        }

        ChatLog chatLog = chatLogMapper.chatLogPostDtoToChatLog(chatMessage);
        chatLog.setChatRoom(chatRoom);

        chatLogService.createChatLog(chatLog);

        ChatLogDto.Response response = chatLogMapper.chatLogToChatLogResponseDto(chatLog);

        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
    }
}
