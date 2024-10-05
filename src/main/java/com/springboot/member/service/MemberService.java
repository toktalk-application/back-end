package com.springboot.member.service;


import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class MemberService {
    private MemberRepository memberRepository;
    private CounselorRepository counselorRepository;
    private PasswordEncoder passwordEncoder;
    private CustomAuthorityUtils customAuthorityUtils;

    public Member createMember(Member member){
        if(!isUserIdAvailable(member.getUserId())){
            throw new BusinessLogicException(ExceptionCode.DUPLICATED_USERID);
        };
        if(!isNicknameAvailable(member.getNickname())){
            throw new BusinessLogicException(ExceptionCode.DUPLICATED_NICKNAME);
        }

        String encryptedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encryptedPassword);

        List<String> roles = customAuthorityUtils.createRoles("hgd@gmail.com");
        member.setRoles(roles);
        return memberRepository.save(member);
    };
    public Member findMember(long memberId){
        return findVerifiedMember(memberId);
    }
    private Member findVerifiedMember(long memberId){
        Optional<Member> optionalMember = memberRepository.findByMemberId(memberId);
        return optionalMember.orElseThrow(() ->
            new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    private boolean isUserIdAvailable(String userId){
        Optional<Member> optionalMember = memberRepository.findByUserId(userId);
        Optional<Counselor> optionalCounselor = counselorRepository.findByUserId(userId);
        return optionalMember.isEmpty() && optionalCounselor.isEmpty();
    }
    private boolean isNicknameAvailable(String nickname){
        return !memberRepository.findByNickname(nickname).isPresent();
    }

    // 검증후 Fcm토큰을 저장하는 메서드
    @Transactional
    public void updateFcmToken(long memberId, String fcmToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        member.setFcmToken(fcmToken);
        memberRepository.save(member);
    }

    // 사용자의 로그인Id를 사용해 사용자 memberId를 조회하는 메서드
    public long getMemberIdByUserId(String username) {
        return memberRepository.findByUserId(username)
                .map(Member::getMemberId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }
}

