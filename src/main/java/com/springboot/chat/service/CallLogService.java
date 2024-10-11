package com.springboot.chat.service;

import com.springboot.chat.entity.CallLog;
import com.springboot.chat.repository.CallLogRepository;
import org.springframework.stereotype.Service;

@Service
public class CallLogService {
    private final CallLogRepository callLogRepository;

    public CallLogService(CallLogRepository callLogRepository) {
        this.callLogRepository = callLogRepository;
    }

    public CallLog createCallLog(CallLog callLog) {
        return callLogRepository.save(callLog);
    }
}
