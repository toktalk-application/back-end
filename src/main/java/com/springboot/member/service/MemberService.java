package com.springboot.member.service;


import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.DailyMood;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.reservation.service.ReservationService;
import com.springboot.utils.CalendarUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Member findMember(String userId){
        return findVerifiedMember(userId);
    }
    private Member findVerifiedMember(long memberId){
        Optional<Member> optionalMember = memberRepository.findByMemberId(memberId);
        return optionalMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }
    private Member findVerifiedMember(String userId){
        Optional<Member> optionalMember = memberRepository.findByUserId(userId);
        return optionalMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    public Member updateMember(Member member){
        Member realMember = findVerifiedMember(member.getMemberId());

        Optional.ofNullable(member.getPassword())
                .ifPresent(password -> {
                    // 같은 비밀번호로 변경 불가능
                    if(passwordEncoder.matches(password, realMember.getPassword())) throw new BusinessLogicException(ExceptionCode.SAME_PASSWORD);
                    realMember.setPassword(passwordEncoder.encode(password));
                });
        Optional.ofNullable(member.getNickname())
                .ifPresent(nickname -> {
                    // 중복 닉네임 검사
                    if(!isNicknameAvailable(nickname)) throw new BusinessLogicException(ExceptionCode.DUPLICATED_NICKNAME);
                    realMember.setNickname(nickname);
                });
        return memberRepository.save(realMember);
    }

    // 회원탈퇴
    public void quitMember(long memberId){
        Member member = findVerifiedMember(memberId);
        member.setMemberStatus(Member.Status.INACTIVE);

        memberRepository.save(member);
    }

    // 아이디 중복 검증
    public boolean isUserIdAvailable(String userId){
        Optional<Member> optionalMember = memberRepository.findByUserId(userId);
        Optional<Counselor> optionalCounselor = counselorRepository.findByUserId(userId);
        return optionalMember.isEmpty() && optionalCounselor.isEmpty();
    }

    // 닉네임 중복 검증
    public boolean isNicknameAvailable(String nickname){
        return !memberRepository.findByNickname(nickname).isPresent();
    }

    // 오늘의 기분 등록
    public void addDailyMood(long memberId, DailyMood mood){
        // 오늘만 등록 가능
        if(!mood.getDate().equals(LocalDate.now())) throw new BusinessLogicException(ExceptionCode.UNAVAILABLE_DATE);

        // 회원 찾아오기
        Member member = findVerifiedMember(memberId);
        // 오늘의 기분이 이미 있으면 지우고 등록
        if(member.getDailyMoods().get(LocalDate.now()) != null) member.getDailyMoods().remove(LocalDate.now());
        mood.setMember(member); // Member <-> DailyMood 양방향 set 메서드
    }

    // 오늘의 기분 월별 조회
    public Map<LocalDate, DailyMood> getMonthlyMoods(long memberId, YearMonth month){
        Member member = findVerifiedMember(memberId);

        return member.getDailyMoods().entrySet().stream()
                .filter(entry -> CalendarUtil.isLocalDateInYearMonth(entry.getKey(), month))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

