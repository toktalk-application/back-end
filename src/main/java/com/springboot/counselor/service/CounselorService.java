package com.springboot.counselor.service;

import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.available_date.AvailableDate;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class CounselorService {
    private final CounselorRepository counselorRepository;
    private final CustomAuthorityUtils customAuthorityUtils;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    public Counselor createCounselor(Counselor counselor){
        if(!isUserIdAvailable(counselor.getUserId())){
            throw new BusinessLogicException(ExceptionCode.DUPLICATED_USERID);
        }

        String encryptedPassword = passwordEncoder.encode(counselor.getPassword());
        counselor.setPassword(encryptedPassword);

        List<String> roles = customAuthorityUtils.createRoles(counselor.getUserId());
        counselor.setRoles(roles);

        // 앞으로 30일 간 예약가능일자 생성
        for(int i = 0; i< 30; i++){
            AvailableDate newDate = new AvailableDate(LocalDate.now().plusDays(i));
            counselor.addAvailableDate(newDate);
        }

        return counselorRepository.save(counselor);
    }
    public Counselor findCounselor(long counselorId){
        return findVerifiedCounselor(counselorId);
    }
    private Counselor findVerifiedCounselor(long counselorId){
        Optional<Counselor> optionalCounselor = counselorRepository.findById(counselorId);
        return optionalCounselor.orElseThrow(() -> new BusinessLogicException(ExceptionCode.COUNSELOR_NOT_FOUND));
    }

    public Counselor updateCounselor(Counselor counselor){
        Counselor realCounselor = findVerifiedCounselor(counselor.getCounselorId());

        Optional.ofNullable(counselor.getPassword())
                .ifPresent(password -> {
                    // 같은 비밀번호로 변경 안 됨
                    if(passwordEncoder.matches(password, realCounselor.getPassword())) throw new BusinessLogicException(ExceptionCode.SAME_PASSWORD);
                    realCounselor.setPassword(passwordEncoder.encode(password));
                });
        Optional.ofNullable(counselor.getPhone())
                .ifPresent(phone -> counselor.setPhone(phone));
        Optional.ofNullable(counselor.getCompany())
                .ifPresent(company -> counselor.setCompany(company));
        Optional.ofNullable(counselor.getName())
                .ifPresent(name -> counselor.setName(name));
        Optional.ofNullable(counselor.getChatPrice())
                .ifPresent(chatprice -> counselor.setChatPrice(chatprice));
        Optional.ofNullable(counselor.getCallPrice())
                .ifPresent(callprice -> counselor.setCallPrice(callprice));

        return counselorRepository.save(realCounselor);
    }
    private boolean isUserIdAvailable(String userId){
        Optional<Member> optionalMember = memberRepository.findByUserId(userId);
        Optional<Counselor> optionalCounselor = counselorRepository.findByUserId(userId);
        return optionalMember.isEmpty() && optionalCounselor.isEmpty();
    }
}
