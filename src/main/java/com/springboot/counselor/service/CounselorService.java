package com.springboot.counselor.service;

import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.counselor.available_date.AvailableTime;
import com.springboot.counselor.dto.CareerDto;
import com.springboot.counselor.dto.CounselorDto;
import com.springboot.counselor.dto.LicenseDto;
import com.springboot.counselor.entity.*;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.utils.CalendarUtil;
import com.springboot.utils.IntValidationUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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

        // 모든 요일에 대해 빈 기본 상담 시간 생성
        for(DayOfWeek day : DayOfWeek.values()){
            DefaultDay defaultDay = new DefaultDay();
            defaultDay.setDayOfWeek(day);
            defaultDay.setCounselor(counselor);
        }
        /*// 앞으로 30일 간 예약가능일자 생성
        for(int i = 0; i< 30; i++){
            AvailableDate newDate = new AvailableDate(LocalDate.now().plusDays(i));
            counselor.addAvailableDate(newDate);
        }*/
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

    // 특정 요일의 기본 상담 시간 수정
    public void setDefaultDays(long counselorId, CounselorDto.DefaultDays patchDto){

        Counselor counselor = findVerifiedCounselor(counselorId);

        // 먼저 뭐가 추가할 타임인지, 뭐가 삭제할 타임인지 판별
        List<LocalTime> currentTimes = counselor.getDefaultDays().get(patchDto.getDayOfWeek()).getStartTimes();
        List<LocalTime> newTimes = patchDto.getTimes();

        Set<LocalTime> additions = new HashSet<>(newTimes);     // currentTimes에는 없고, newTimes에는 있음
        Set<LocalTime> unchanged = new HashSet<>();             // 양쪽에 다 있음
        Set<LocalTime> removes = new HashSet<>(currentTimes);   // currentTimes에는 있고, newTimes에는 없음

        // 각각을 순회하면서 양쪽에 다 있는 경우에만 unchanged에 추가
        for (LocalTime time : currentTimes) {
            if (newTimes.contains(time)) {                      // 순회가 끝나면 최종적으로
                unchanged.add(time);                            // unchanged = unchanged
                additions.remove(time);                         // additions = newTimes - unchanged
                removes.remove(time);                           // removes = currentTimes - unchanged 가 됨
            }
        }

        // 해당 요일에 대해 Counselor의 Default 상담시간 업데이트
        DefaultDay defaultDay = counselor.getDefaultDays().get(patchDto.getDayOfWeek());
        // 먼저 기존의 DefaultTimeSlot 비우기
        defaultDay.getDefaultTimeSlots().clear();
        // LocalTime들을 DefaultTimeSlot으로 변환 후 DefaultDay에 등록
        for(LocalTime time : patchDto.getTimes()){
            DefaultTimeSlot timeSlot = new DefaultTimeSlot();
            timeSlot.setStartTime(time);
            timeSlot.setEndTime(time.plusMinutes(50));
            timeSlot.setDefaultDay(defaultDay); // 하나씩 등록 (양방향 set메서드)
        }

        counselorRepository.save(counselor);

        /*// 수정된 정보에 맞게 실제 AvailableTimes 목록 변경
        updateAvailableTimes(counselor, patchDto.getDayOfWeek(), additions, removes);*/

        /*// 설정한 default time 정보에 따라 자동으로 AvailableTime 생성
        addAvailableTimes(counselorId, 2);*/
    }

    // 상담사의 이번 달 포함 n달 치 AvailableTimes 추가
    public void addAvailableTimes(long counselorId, int months){
        Counselor counselor = findVerifiedCounselor(counselorId);

        // months는 자연수만 들어와야 함
        if(months < 1) throw new BusinessLogicException(ExceptionCode.INVALID_MONTH_PARAMETER);
        // 한계일 설정 ({month - 1} 개월 뒤의 마지막 날짜)
        LocalDate limitDate = LocalDate.now().withDayOfMonth(1).plusMonths(months).minusDays(1);
        // 범위 내에서 각 요일에 대한 기본 시간표를 바탕으로 AvailableTime 생성
        counselor.getDefaultDays().forEach((dayOfWeek, defaultDay) -> {
            // 기준일 설정 (오늘 날짜 기준 가장 가까운 해당 요일로 초기화)
            LocalDate refDate = CalendarUtil.getNextDateOfCertainDayOfWeek(dayOfWeek);
            while(refDate.isBefore(limitDate)){
                // AvailableDate 생성 및 등록
                /*AvailableDate targetDate = counselor.getAvailableDates().get(refDate);*/
                AvailableDate targetDate = new AvailableDate();
                targetDate.setDate(refDate);
                targetDate.setCounselor(counselor); // Counselor <-> AvailableDate 양방향 set 메서드
                // AvailableTime 생성 및 등록
                defaultDay.getStartTimes().forEach(time -> {
                    AvailableTime newAvailableTime = new AvailableTime();
                    newAvailableTime.setStartTime(time);
                    newAvailableTime.setEndTime(time.plusMinutes(50));
                    newAvailableTime.setAvailableDate(targetDate); // AvailableDate <-> AvailableTime 양방향 set 메서드
                });
                refDate = refDate.plusDays(7);
            }
        });
        /*// 변경사항 저장 (필요 없을듯?)
        counselorRepository.save(counselor);*/
    }

    // 상담사의 특정 요일 AvailableTimes 정보 변경
    private void updateAvailableTimes(Counselor counselor, DayOfWeek dayOfWeek, Set<LocalTime> additions, Set<LocalTime> removes){
        List<AvailableDate> dates = counselor.getAvailableDatesInCertainDayOfWeek(dayOfWeek);
        // 해당 요일의 모든 AvailableDate에 대해
        for(AvailableDate date : dates){
            // additions에 해당하는 시간 추가
            for(LocalTime addition : additions){
                AvailableTime newTime = new AvailableTime();
                newTime.setStartTime(addition);
                newTime.setEndTime(addition.plusMinutes(50));
                newTime.setAvailableDate(date); // 양방향 set 메서드
            }
            // 그리고 removes에 해당하는 시간 삭제
            for(LocalTime remove : removes){
                // 이 시간에 이미 잡혀 있는 예약이 있으면 삭제할 수 없음
                if(date.getAvailableTimes().get(remove).getReservation() != null) throw new BusinessLogicException(ExceptionCode.TIMESLOT_DELETION_DENIED);
                date.getAvailableTimes().remove(remove);
            }
        }
        counselorRepository.save(counselor);
    }

    // 특정 요일에 대한 기본 상담 시간 조회
    public List<LocalTime> getDefaultTimesOfDay(long counselorId, DayOfWeek dayOfWeek){
        Counselor counselor = findVerifiedCounselor(counselorId);
        DefaultDay defaultDay = counselor.getDefaultDays().get(dayOfWeek);

        List<LocalTime> times = defaultDay.getDefaultTimeSlots().stream()
                .map(timeslot -> timeslot.getStartTime()).toList();

        return times;
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
