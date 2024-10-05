package com.springboot.firebase.controller;

import com.springboot.firebase.dto.FcmSendDto;
import com.springboot.firebase.service.MyFirebaseMessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/fcm")
public class FcmController {
    private final MyFirebaseMessagingService fcmService;

    public FcmController(MyFirebaseMessagingService fcmService) {
        this.fcmService = fcmService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody FcmSendDto request) {
        fcmService.sendMessage(request);
        return ResponseEntity.ok("Notification sent successfully");
    }
}
