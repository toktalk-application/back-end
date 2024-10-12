package com.springboot.chat.repository;

import com.springboot.chat.entity.ChatRoom;
import com.springboot.counselor.entity.Counselor;
import com.springboot.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByMemberAndCounselor(Member member, Counselor counselor);
    List<ChatRoom> findByMember(Member member);
    List<ChatRoom> findByCounselor(Counselor counselor);
}
