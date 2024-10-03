package com.springboot.counselor.service;

import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.counselor.dto.CareerDto;
import com.springboot.counselor.dto.CounselorDto;
import com.springboot.counselor.dto.LicenseDto;
import com.springboot.counselor.entity.Career;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.entity.Keyword;
import com.springboot.counselor.entity.License;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.utils.IntValidationUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public Counselor createCounselor(Counselor counselor, CounselorDto.Post postDto){
        // 자격증, 경력사항은 최소 하나 ~ 최대 3개
        int licenceSize = postDto.getLicenses() == null ? 0 : postDto.getLicenses().size();
        if(licenceSize == 0 || licenceSize > 3) throw new BusinessLogicException(ExceptionCode.LICENSE_AMOUNT_VIOLATION);
        int careerSize = postDto.getCareers() == null ? 0 : postDto.getCareers().size();
        if(careerSize == 0 || careerSize > 3) throw new BusinessLogicException(ExceptionCode.CAREER_AMOUNT_VIOLATION);

        // 아이디 중복 검사
        if(!isUserIdAvailable(counselor.getUserId()))throw new BusinessLogicException(ExceptionCode.DUPLICATED_USERID);

        String encryptedPassword = passwordEncoder.encode(counselor.getPassword());
        counselor.setPassword(encryptedPassword);

        List<String> roles = customAuthorityUtils.createRoles(counselor.getUserId());
        counselor.setRoles(roles);

        // 자격증 등록
        counselor.getLicenses().clear(); // dto와 mapper 때문에 생긴 거 지우기
        postDto.getLicenses().forEach(license -> {
            counselor.addLicense(license);
        });
        // 경력사항 등록
        counselor.getCareers().clear(); // dto와 mapper 때문에 생긴 거 지우기
        postDto.getCareers().forEach(career -> {
            counselor.addCareer(career);
        });

        // 앞으로 30일 간 예약가능일자 생성
        for(int i = 0; i< 30; i++){
            AvailableDate newDate = new AvailableDate(LocalDate.now().plusDays(i));
            counselor.addAvailableDate(newDate);
        }
        return counselorRepository.save(counselor);
    }
    // 자격증 추가
    public void addLicense(long counselorId, List<LicenseDto.Post> postDtos){
        Counselor counselor = findVerifiedCounselor(counselorId);
        // 3개 넘개는 추가 안됨
        if(counselor.getLicenses().size() + postDtos.size() > 3) throw new BusinessLogicException(ExceptionCode.LICENSE_AMOUNT_VIOLATION);

        // 자격증 등록
        postDtos.forEach(postDto -> {
            License license = new License();
            license.setCounselor(counselor);
            license.setLicenseName(postDto.getLicenseName());
            license.setOrganization(postDto.getOrganization());
            license.setIssueDate(postDto.getIssueDate());
        });
        counselor.setModifiedAt(LocalDateTime.now());
    }
    // 자격증 삭제
    public void deleteLicense(long counselorId, int licenseNumber){
        Counselor counselor = findVerifiedCounselor(counselorId);
        List<License> licenses = counselor.getLicenses();

        // 딱 하나 남아 있는데 삭제할 수 없음
        if(licenses.size() == 1) throw new BusinessLogicException(ExceptionCode.LICENSE_AMOUNT_VIOLATION);

        // 전달받은 licenseNumber 번째의 자격증 삭제
        if(!IntValidationUtil.isIntInRange(licenseNumber, 1, licenses.size())) throw new BusinessLogicException(ExceptionCode.LICENSE_NOT_FOUND);
        licenses.remove(licenseNumber - 1);

        counselor.setModifiedAt(LocalDateTime.now());
        counselorRepository.save(counselor);
    }
    // 경력사항 추가
    public void addCareer(long counselorId, List<CareerDto.Post> postDtos){
        Counselor counselor = findVerifiedCounselor(counselorId);
        // 3개 넘개는 추가 안됨
        if(counselor.getLicenses().size() + postDtos.size() > 3) throw new BusinessLogicException(ExceptionCode.LICENSE_AMOUNT_VIOLATION);

        // 경력사항 등록
        postDtos.forEach(postDto -> {
            Career career = new Career();
            career.setCounselor(counselor);
            career.setCompany(postDto.getCompany());
            career.setResponsibility(postDto.getResponsibility());
            career.setClassification(postDto.getClassification());
        });
        counselor.setModifiedAt(LocalDateTime.now());
    }

    // 상담사 태그 추가
    public void addKeyword(long counselorId, String word){
        Counselor counselor = findVerifiedCounselor(counselorId);

        Keyword keyword = new Keyword();
        keyword.setWord(word);
        keyword.setCounselor(counselor);

        counselorRepository.save(counselor);
    }

    // 경력사항 삭제
    public void deleteCareer(long counselorId, int careerNumber){
        Counselor counselor = findVerifiedCounselor(counselorId);
        List<Career> careers = counselor.getCareers();

        // 딱 하나 남아 있는데 삭제할 수 없음
        if(careers.size() == 1) throw new BusinessLogicException(ExceptionCode.LICENSE_AMOUNT_VIOLATION);

        // 전달받은 careerNumber 번째의 경력사항 삭제
        if(!IntValidationUtil.isIntInRange(careerNumber, 1, careers.size())) throw new BusinessLogicException(ExceptionCode.CAREER_NOT_FOUND);
        careers.remove(careerNumber - 1);

        counselorRepository.save(counselor);
        counselor.setModifiedAt(LocalDateTime.now());
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
                .ifPresent(phone -> realCounselor.setPhone(phone));
        Optional.ofNullable(counselor.getCompany())
                .ifPresent(company -> realCounselor.setCompany(company));
        Optional.ofNullable(counselor.getName())
                .ifPresent(name -> realCounselor.setName(name));
        Optional.ofNullable(counselor.getChatPrice())
                .ifPresent(chatprice -> realCounselor.setChatPrice(chatprice));
        Optional.ofNullable(counselor.getCallPrice())
                .ifPresent(callprice -> realCounselor.setCallPrice(callprice));

        counselor.setModifiedAt(LocalDateTime.now());
        return counselorRepository.save(realCounselor);
    }
    private boolean isUserIdAvailable(String userId){
        Optional<Member> optionalMember = memberRepository.findByUserId(userId);
        Optional<Counselor> optionalCounselor = counselorRepository.findByUserId(userId);
        return optionalMember.isEmpty() && optionalCounselor.isEmpty();
    }
}
