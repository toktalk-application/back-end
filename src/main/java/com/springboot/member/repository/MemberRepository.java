package com.springboot.member.repository;


import com.springboot.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberId(long memberId);
    @EntityGraph(attributePaths = {"roles"})
    Optional<Member> findByUserId(String userId);
    Optional<Member> findByNickname(String nickname);

}
