package com.springboot.chat.controller;

import com.springboot.chat.dto.CallMessage;
import com.springboot.chat.entity.ChatRoom;
import com.springboot.chat.service.ChatRoomService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class CallController {

    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    public CallController(ChatRoomService chatRoomService, SimpMessagingTemplate messagingTemplate) {
        this.chatRoomService = chatRoomService;
        this.messagingTemplate = messagingTemplate;
    }

    // 통화 요청
    @MessageMapping("/call/start/{roomId}")
    public void startCall(@DestinationVariable long roomId, CallMessage callMessage) {
        ChatRoom chatRoom = chatRoomService.findVerifiedChatRoom(roomId);
        chatRoom.setCallStatus(ChatRoom.CallStatus.CONNECTING);
        chatRoomService.updateChatRoom(chatRoom);

        // 수신자에게 통화 요청 전달
        messagingTemplate.convertAndSend("/topic/call/" + roomId, callMessage);
    }

    // 통화 수락
    @MessageMapping("/call/accept/{roomId}")
    public void acceptCall(@DestinationVariable long roomId, CallMessage callMessage) {
        ChatRoom chatRoom = chatRoomService.findVerifiedChatRoom(roomId);
        chatRoom.setCallStatus(ChatRoom.CallStatus.ACTIVE);
        chatRoomService.updateChatRoom(chatRoom);

        // 발신자에게 통화 수락 알림
        messagingTemplate.convertAndSend("/topic/call/" + roomId, callMessage);
    }

    // 통화 거절
    @MessageMapping("/call/reject/{roomId}")
    public void rejectCall(@DestinationVariable long roomId, CallMessage callMessage) {
        ChatRoom chatRoom = chatRoomService.findVerifiedChatRoom(roomId);
        chatRoom.setCallStatus(ChatRoom.CallStatus.INACTIVE);
        chatRoomService.updateChatRoom(chatRoom);

        // 발신자에게 통화 거절 알림
        messagingTemplate.convertAndSend("/topic/call/" + roomId, callMessage);
    }

    // 통화 종료
    @MessageMapping("/call/end/{roomId}")
    public void endCall(@DestinationVariable long roomId, CallMessage callMessage) {
        ChatRoom chatRoom = chatRoomService.findVerifiedChatRoom(roomId);
        chatRoom.setCallStatus(ChatRoom.CallStatus.INACTIVE);
        chatRoomService.updateChatRoom(chatRoom);

        // 상대방에게 통화 종료 알림
        messagingTemplate.convertAndSend("/topic/call/" + roomId, callMessage);
    }

    // 시그널링 메시지 처리
    @MessageMapping("/call/signal/{roomId}")
    public void signaling(@DestinationVariable long roomId, CallMessage callMessage) {
        // 상대방에게 시그널링 메시지 전달
        messagingTemplate.convertAndSend("/topic/call/signal/" + roomId, callMessage);
    }
}
