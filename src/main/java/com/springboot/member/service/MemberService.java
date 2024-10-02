package com.springboot.member.service;


import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.entity.Review;
import com.springboot.reservation.service.ReservationService;
import com.springboot.utils.CredentialUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
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

    private boolean isUserIdAvailable(String userId){
        Optional<Member> optionalMember = memberRepository.findByUserId(userId);
        Optional<Counselor> optionalCounselor = counselorRepository.findByUserId(userId);
        return optionalMember.isEmpty() && optionalCounselor.isEmpty();
    }
    private boolean isNicknameAvailable(String nickname){
        return !memberRepository.findByNickname(nickname).isPresent();
    }
}

