package com.springboot.chat.service;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.chat.entity.ChatRoom;
import com.springboot.chat.repository.ChatRoomRepository;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.utils.CredentialUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberService memberService;
    private final CounselorService counselorService;

    public ChatRoomService(ChatRoomRepository chatRoomRepository, MemberService memberService, CounselorService counselorService) {
        this.chatRoomRepository = chatRoomRepository;
        this.memberService = memberService;
        this.counselorService = counselorService;
    }

    public ChatRoom createOrGetChatRoom(long memberId, Authentication authentication) {

        Member findMember = memberService.findMember(memberId);
        Counselor findCounselor = counselorService.findCounselor(
                Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"))
        );

        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findByMemberAndCounselor(findMember, findCounselor);

        ChatRoom chatRoom;
        if (optionalChatRoom.isPresent()) {
            chatRoom = optionalChatRoom.get();
            chatRoom.setRoomStatus(ChatRoom.RoomStatus.OPEN);
        } else {
            chatRoom = new ChatRoom();
            chatRoom.setMember(findMember);
            chatRoom.setCounselor(findCounselor);
            chatRoom.setRoomStatus(ChatRoom.RoomStatus.OPEN);
            chatRoom.setCreatedAt(LocalDateTime.now());
        }
        return chatRoomRepository.save(chatRoom);
    }

    public ChatRoom closeChatRoom(long roomId, Authentication authentication) {
        Counselor findCounselor = counselorService.findCounselor(
                Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"))
        );

        ChatRoom chatRoom = findVerifiedChatRoom(roomId);

        if (!findCounselor.equals(chatRoom.getCounselor())) {
            throw new BusinessLogicException(ExceptionCode.ACCESS_DENIED);
        }
        chatRoom.setRoomStatus(ChatRoom.RoomStatus.CLOSE);
        chatRoom.setCallStatus(ChatRoom.CallStatus.INACTIVE);
        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }

    public ChatRoom updateChatRoom(ChatRoom chatRoom) {
        return chatRoomRepository.save(chatRoom);
    }

    public ChatRoom findChatRoom(long roomId, Authentication authentication) {
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;

        if(auth.getUserType() == LoginDto.UserType.MEMBER) {

            Member findMember = memberService.findMember(
                    Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"))
            );

            ChatRoom chatRoom = findVerifiedChatRoom(roomId);

            if (chatRoom.getMember().getMemberId() != findMember.getMemberId()) {
                throw new BusinessLogicException(ExceptionCode.ACCESS_DENIED);
            }

            return findVerifiedChatRoom(roomId);
        } else {
            Counselor findCounselor = counselorService.findCounselor(
                    Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"))
            );

            ChatRoom chatRoom = findVerifiedChatRoom(roomId);

            if(chatRoom.getCounselor().getCounselorId() != findCounselor.getCounselorId()) {
                throw new BusinessLogicException(ExceptionCode.ACCESS_DENIED);
            }

            return findVerifiedChatRoom(roomId);
        }
    }

    public List<ChatRoom> findChatRooms(Authentication authentication) {
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;

        if(auth.getUserType() == LoginDto.UserType.MEMBER) {

            Member findMember = memberService.findMember(
                    Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"))
            );

            return chatRoomRepository.findByMember(findMember);
        } else {
            Counselor findCounselor = counselorService.findCounselor(
                    Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"))
            );

            return chatRoomRepository.findByCounselor(findCounselor);
        }
    }

    public ChatRoom findVerifiedChatRoom(long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CHAT_ROOM_NOT_FOUND));
    }
}
