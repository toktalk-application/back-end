package com.springboot.chat.repository;

import com.springboot.chat.entity.ChatLog;
import com.springboot.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
}
