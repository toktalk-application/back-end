package com.springboot.chat.controller;

import com.springboot.chat.dto.ChatRoomDto;
import com.springboot.chat.entity.ChatRoom;
import com.springboot.chat.mapper.ChatLogMapper;
import com.springboot.chat.mapper.ChatRoomMapper;
import com.springboot.chat.service.ChatRoomService;
import com.springboot.firebase.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/chat_rooms")
@Validated
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatLogMapper chatLogMapper;
    private final NotificationService notificationService;

    public ChatRoomController(ChatRoomService chatRoomService, ChatRoomMapper chatRoomMapper, ChatLogMapper chatLogMapper, NotificationService notificationService) {
        this.chatRoomService = chatRoomService;
        this.chatRoomMapper = chatRoomMapper;
        this.chatLogMapper = chatLogMapper;
        this.notificationService = notificationService;
    }
    @PostMapping("/open")
    public ResponseEntity<?> openChatRoom(@RequestParam long memberId, Authentication authentication) {
        ChatRoom chatRoom = chatRoomService.createOrGetChatRoom(memberId, authentication);
        ChatRoomDto.DetailResponse responseDto = chatRoomMapper.chatRoomToChatRoomDetailResponseDto(chatRoom, chatLogMapper);

        notificationService.sendChatRoomCreationNotification(memberId, chatRoom.getRoomId());
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{room-id}/close")
    public ResponseEntity<?> closeChatRoom(@PathVariable("room-id") @Positive long roomId, Authentication authentication) {
        ChatRoom chatRoom = chatRoomService.closeChatRoom(roomId, authentication);
        ChatRoomDto.DetailResponse responseDto = chatRoomMapper.chatRoomToChatRoomDetailResponseDto(chatRoom, chatLogMapper);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("{room-id}")
    public ResponseEntity getChatRoom(@PathVariable("room-id") @Positive long roomId, Authentication authentication) {
        ChatRoom chatRoom = chatRoomService.findChatRoom(roomId, authentication);
        ChatRoomDto.DetailResponse responseDto = chatRoomMapper.chatRoomToChatRoomDetailResponseDto(chatRoom, chatLogMapper);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity getChatRooms(Authentication authentication) {
        List<ChatRoom> chatRooms = chatRoomService.findChatRooms(authentication);

        return ResponseEntity.ok(chatRoomMapper.chatRoomsToChatRoomSimpleResponseDtos(chatRooms));
    }
}
