package com.springboot.chat.controller;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.springboot.chat.dto.ChatLogDto;
import com.springboot.chat.entity.ChatLog;
import com.springboot.chat.entity.ChatRoom;
import com.springboot.chat.service.ChatLogService;
import com.springboot.chat.service.ChatRoomService;
import com.springboot.chat.mapper.ChatLogMapper;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ChatController {

    private final SocketIOServer server;
    private final ChatLogService chatLogService;
    private final ChatRoomService chatRoomService;
    private final ChatLogMapper chatLogMapper;

    public ChatController(SocketIOServer server,
                          ChatLogService chatLogService,
                          ChatRoomService chatRoomService,
                          ChatLogMapper chatLogMapper) {
        this.server = server;
        this.chatLogService = chatLogService;
        this.chatRoomService = chatRoomService;
        this.chatLogMapper = chatLogMapper;
    }

    @PostConstruct
    public void init() {
        server.addConnectListener(this::onConnectEvent);
        server.addDisconnectListener(this::onDisconnectEvent);
        server.addEventListener("joinRoom", Long.class, this::onJoinRoom);
        server.addEventListener("message", ChatLogDto.Post.class, this::onChatMessage);
    }

    private void onConnectEvent(SocketIOClient client) {
        System.out.println("Client connected: " + client.getSessionId());
    }

    private void onDisconnectEvent(SocketIOClient client) {
        System.out.println("Client disconnected: " + client.getSessionId());
    }

    private void onJoinRoom(SocketIOClient client, Long roomId, AckRequest ackRequest) {
        client.joinRoom(roomId.toString());
        System.out.println("Client joined room: " + roomId);
    }

    private void onChatMessage(SocketIOClient client, ChatLogDto.Post chatMessage, AckRequest ackRequest) {
        System.out.println("Received message from client: " + chatMessage.getMessage());

        try {
            ChatRoom chatRoom = chatRoomService.findVerifiedChatRoom(chatMessage.getRoomId());

            if (chatRoom.getRoomStatus() == ChatRoom.RoomStatus.CLOSE) {
                throw new BusinessLogicException(ExceptionCode.CHAT_ROOM_CLOSED);
            }

            ChatLog chatLog = chatLogMapper.chatLogPostDtoToChatLog(chatMessage);
            chatLog.setChatRoom(chatRoom);
            ChatLog savedChatLog = chatLogService.createChatLog(chatLog);

            ChatLogDto.ChatResponse response = chatLogMapper.chatLogToChatLogChatResponseDto(savedChatLog);

            // 방의 모든 클라이언트에게 메시지 브로드캐스트
            server.getRoomOperations(String.valueOf(chatMessage.getRoomId())).sendEvent("message", response);

            // 메시지 전송 성공 확인을 클라이언트에게 전송 (선택적)
            if (ackRequest.isAckRequested()) {
                ackRequest.sendAckData(response);
            }
        } catch (BusinessLogicException e) {
            // 에러 발생 시 전송자에게 에러 메시지 전송
            client.sendEvent("error", e.getMessage());

            // 방의 다른 클라이언트들에게도 에러 알림 (선택적)
            server.getRoomOperations(String.valueOf(chatMessage.getRoomId()))
                    .sendEvent("error", "메시지 전송 중 오류가 발생했습니다.");
        }
    }
}
