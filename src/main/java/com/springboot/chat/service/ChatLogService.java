package com.springboot.chat.service;

import com.springboot.chat.entity.ChatLog;
import com.springboot.chat.entity.ChatRoom;
import com.springboot.chat.repository.ChatLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatLogService {

    private final ChatLogRepository chatLogRepository;

    public ChatLogService(ChatLogRepository chatLogRepository) {
        this.chatLogRepository = chatLogRepository;
    }

    public ChatLog createChatLog(ChatLog chatLog) {
        return chatLogRepository.save(chatLog);
    }
}