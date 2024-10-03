package com.springboot.member.service;


import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.reservation.service.ReservationService;
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
    private ReservationService reservationService;

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

    // 우울증 테스트 결과 생성
    public void createTest(long memberId, MemberDto.Test testDto){
        Member member = findVerifiedMember(memberId);

        int totalScore = 0;
        for(int score : testDto.getAnswers()){
            totalScore += score;
        }
        member.setDepressionScore(totalScore);
        memberRepository.save(member);
    }
    public Member findMember(long memberId){
        return findVerifiedMember(memberId);
    }
    private Member findVerifiedMember(long memberId){
        Optional<Member> optionalMember = memberRepository.findByMemberId(memberId);
        return optionalMember.orElseThrow(() ->
            new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    public Member updateMember(Member member){
        Member realMember = findVerifiedMember(member.getMemberId());

        Optional.ofNullable(member.getPassword())
                .ifPresent(password -> {
                    // 같은 비밀번호로 변경 불가능
                    if(passwordEncoder.matches(password, realMember.getPassword())) throw new BusinessLogicException(ExceptionCode.SAME_PASSWORD);
                    realMember.setPassword(passwordEncoder.encode(password));
                });
        return memberRepository.save(realMember);
    }

    public void quitMember(long memberId){
        Member member = findVerifiedMember(memberId);
        member.setMemberStatus(Member.Status.INACTIVE);

        memberRepository.save(member);
    }

    private boolean isUserIdAvailable(String userId){
        Optional<Member> optionalMember = memberRepository.findByUserId(userId);
        Optional<Counselor> optionalCounselor = counselorRepository.findByUserId(userId);
        return optionalMember.isEmpty() && optionalCounselor.isEmpty();
    }
    private boolean isNicknameAvailable(String nickname){
        return !memberRepository.findByNickname(nickname).isPresent();
    }
}

