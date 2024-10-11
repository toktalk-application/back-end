package com.springboot.chat.controller;

import com.springboot.chat.entity.CallLog;
import com.springboot.chat.entity.ChatRoom;
import com.springboot.chat.service.CallLogService;
import com.springboot.chat.service.ChatRoomService;
import com.springboot.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/call")
public class CallLogController {

    private final S3Service s3Service;
    private final CallLogService callLogService;
    private final ChatRoomService chatRoomService;

    public CallLogController(S3Service s3Service, CallLogService callLogService, ChatRoomService chatRoomService) {
        this.s3Service = s3Service;
        this.callLogService = callLogService;
        this.chatRoomService = chatRoomService;
    }

    // 녹음 파일 업로드
    @PostMapping("/upload/{roomId}")
    public ResponseEntity<?> uploadRecording(@PathVariable long roomId,
                                             @RequestParam("file") MultipartFile file) {
        // 채팅방 확인
        ChatRoom chatRoom = chatRoomService.findVerifiedChatRoom(roomId);

        // 파일 업로드
        String fileUrl = s3Service.uploadFile(file);

        // CallLog 생성 및 저장
        CallLog callLog = new CallLog();
        callLog.setChatRoom(chatRoom);
        callLog.setFileUrl(fileUrl);
        callLogService.createCallLog(callLog);

        return ResponseEntity.ok(fileUrl);
    }
}
