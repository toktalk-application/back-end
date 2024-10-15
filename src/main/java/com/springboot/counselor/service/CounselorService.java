package com.springboot.counselor.service;

import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.counselor.available_date.AvailableTime;
import com.springboot.counselor.dto.AvailableDateDto;
import com.springboot.counselor.dto.CounselorDto;
import com.springboot.counselor.entity.*;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.repository.ReservationRepository;
import com.springboot.reservation.service.ReservationService;
import com.springboot.utils.CalendarUtil;
import com.springboot.utils.IntValidationUtil;
import com.springboot.utils.TimeUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CounselorService {
    private final CounselorRepository counselorRepository;
    private final CustomAuthorityUtils customAuthorityUtils;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public CounselorService(CounselorRepository counselorRepository,
                            CustomAuthorityUtils customAuthorityUtils,
                            PasswordEncoder passwordEncoder,
                            MemberRepository memberRepository,
                            ReservationRepository reservationRepository,
                            @Lazy ReservationService reservationService) {
        this.counselorRepository = counselorRepository;
        this.customAuthorityUtils = customAuthorityUtils;
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

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
    public void addLicense(long counselorId, List<License> licenses){
        Counselor counselor = findVerifiedCounselor(counselorId);
        // 3개 넘개는 추가 안됨
        if(counselor.getLicenses().size() + licenses.size() > 3) throw new BusinessLogicException(ExceptionCode.LICENSE_AMOUNT_VIOLATION);

        // 자격증 등록
        licenses.forEach(license -> {
            license.setCounselor(counselor); // Counselor <-> License 양방향 set 메서드
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
    public void addCareer(long counselorId, List<Career> careers){
        Counselor counselor = findVerifiedCounselor(counselorId);
        // 3개 넘개는 추가 안됨
        if(counselor.getCareers().size() + careers.size() > 3) throw new BusinessLogicException(ExceptionCode.CAREER_AMOUNT_VIOLATION);

        // 경력사항 등록
        careers.forEach(career -> {
            career.setCounselor(counselor); // Counselor <-> Career 양방향 set 메서드
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

    // 단일 경력사항 삭제
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
    public void setDefaultDays(long counselorId, CounselorDto.DefaultDays dto, boolean isInitialization){

        Counselor counselor = findVerifiedCounselor(counselorId);

        // 먼저 뭐가 추가할 타임인지, 뭐가 삭제할 타임인지 판별
        List<LocalTime> currentTimes = counselor.getDefaultDays().get(dto.getDayOfWeek()).getStartTimes();
        List<LocalTime> newTimes = dto.getTimes();

        TimeUtils.TimeComparisonResult comparisonResult = TimeUtils.compare(currentTimes, newTimes);
        Set<LocalTime> additions = comparisonResult.getAdditions();
        Set<LocalTime> removes = comparisonResult.getRemoved();

        // 해당 요일에 대해 Counselor의 Default 상담시간 업데이트
        DefaultDay defaultDay = counselor.getDefaultDays().get(dto.getDayOfWeek());
        // 먼저 기존의 DefaultTimeSlot 비우기
        defaultDay.getDefaultTimeSlots().clear();
        // LocalTime들을 DefaultTimeSlot으로 변환 후 DefaultDay에 등록
        for(LocalTime time : dto.getTimes()){
            DefaultTimeSlot timeSlot = new DefaultTimeSlot();
            timeSlot.setStartTime(time);
            timeSlot.setEndTime(time.plusMinutes(50));
            timeSlot.setDefaultDay(defaultDay); // 하나씩 등록 (DefaultDay <-> DefaultTimeslot 양방향 set메서드)
        }

        // 수정된 정보에 맞게 실제 AvailableTimes 목록 변경
        if(!isInitialization){
            updateAvailableTimes(counselor, dto.getDayOfWeek(), additions, removes);
        }
    }

    // default days 초기화 여부 조회
    public boolean areDefaultDaysInitialized(long counselorId){
        Counselor counselor = findVerifiedCounselor(counselorId);
        return !counselor.getAvailableDates().isEmpty();
    }

    // 상담사의 이번 달 포함 n달 치 AvailableTimes 추가 (새로 추가)
    public void addInitialAvailableTimes(long counselorId, int months){
        Counselor counselor = findVerifiedCounselor(counselorId);
        // months는 자연수만 들어와야 함
        if(months < 1) throw new BusinessLogicException(ExceptionCode.INVALID_MONTH_PARAMETER);
        // 한계일 설정 ({month - 1} 개월 뒤의 마지막 날짜)
        LocalDate limitDate = LocalDate.now().withDayOfMonth(1).plusMonths(months).minusDays(1);
        addAvailableTimes(counselor, LocalDate.now(), limitDate);
    }

    // 달이 넘어갈 때 다다음 달의 AvailableTimes 추가
    @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 0시 0분 0초에 실행
    public void addExtraAvailableTimes(){
        LocalDate startDate = LocalDate.now().plusMonths(1);
        LocalDate limitDate = LocalDate.now().plusMonths(2).minusDays(1);
        // 모든 상담사에 대해 실행
        counselorRepository.findAll().forEach(counselor -> {
            // 물론 유효한 상담사들에 한해서
            if(counselor.getCounselorStatus().equals(Counselor.Status.ACTIVE)){
                addAvailableTimes(counselor, startDate, limitDate);
            }
        });
    }
    private void addAvailableTimes(Counselor counselor, LocalDate startDate, LocalDate limitDate){
        // 범위 내에서 각 요일에 대한 기본 시간표를 바탕으로 AvailableTime 생성
        counselor.getDefaultDays().forEach((dayOfWeek, defaultDay) -> {
            // 기준일 설정 (오늘 날짜 기준 가장 가까운 해당 요일로 초기화)
            LocalDate refDate = CalendarUtil.getNextDateOfCertainDayOfWeek(dayOfWeek, startDate);
            while(refDate.isBefore(limitDate)){
                // AvailableDate 생성 및 등록
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
                newTime.setAvailableDate(date); // AvailableDate <-> AvailableTime 양방향 set 메서드
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
    // 특정 날짜에 대한 AvailableDate 조회
    public AvailableDate getAvailableDate(long counselorId, LocalDate date){
        Counselor counselor = findVerifiedCounselor(counselorId);

        Optional<AvailableDate> optionalAvailableDate = Optional.ofNullable(counselor.getAvailableDate(date));
        return optionalAvailableDate.orElseThrow(() -> new BusinessLogicException(ExceptionCode.UNAVAILABLE_DATE));
    }
    // 특정 날짜에 대한 예약 가능한 시간만을 포함하는 AvailableDate 조회
    public AvailableDate getFilteredAvailableDate(long counselorId, LocalDate date){
        Counselor counselor = findVerifiedCounselor(counselorId);

        Optional<AvailableDate> optionalAvailableDate = Optional.ofNullable(counselor.getAvailableDate(date));
        AvailableDate availableDate = optionalAvailableDate.orElseThrow(() -> new BusinessLogicException(ExceptionCode.UNAVAILABLE_DATE));

        // 예약 잡힌 시간은 빼고 반환
        Map<LocalTime, AvailableTime> filteredAvailableTimes = availableDate.getAvailableTimes().entrySet().stream()
                .filter(entry -> entry.getValue().getReservation() == null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        // cascade orphan removal 땜에 새로 만들어서 반환
        AvailableDate tempAvailableDate = new AvailableDate();
        tempAvailableDate.setAvailableTimes(filteredAvailableTimes);
        return tempAvailableDate;
    }
    // 특정 날짜에 대한 AvailableDate 수정
    public void updateAvailableDate(long counselorId, AvailableDateDto.Patch patchDto){
        LocalDate date = patchDto.getDate();
        Counselor counselor = findVerifiedCounselor(counselorId);
        AvailableDate availableDate = counselor.getAvailableDate(date);
        // 먼저 해당일의 AvailableTimes를 가져옴
        Map<LocalTime, AvailableTime> availableTimes = counselor.getAvailableDate(date).getAvailableTimes();
        List<LocalTime> currentTimes = new ArrayList<>(availableTimes.keySet());
        // 뭐가 추가된 것이고 삭제된 것인지 판별
        TimeUtils.TimeComparisonResult comparisonResult = TimeUtils.compare(currentTimes, patchDto.getTimes());
        // 추가된 건 새로 만들어서 넣기
        comparisonResult.getAdditions().forEach(time -> {
            AvailableTime availableTime = new AvailableTime();
            availableTime.setStartTime(time);
            availableTime.setEndTime(time.plusMinutes(50));
            availableTime.setAvailableDate(availableDate); // AvailableDate <-> AvailableTime 양방향 set메서드
        });
        // 삭제된 건 제거
        comparisonResult.getRemoved().forEach(time -> {
            AvailableTime availableTime = availableDate.getAvailableTimes().get(time);
            // 예약 있으면 제거 불가
            if(availableTime.getReservation() != null) throw new BusinessLogicException(ExceptionCode.TIMESLOT_DELETION_DENIED);
            // 예약 없으면 제거 진행
            availableDate.getAvailableTimes().remove(time);
        });
        // 변경사항 저장
        counselorRepository.save(counselor);
    }

    // 특정 요일에 대한 기본 상담 시간 조회
    public List<LocalTime> getDefaultTimesOfDay(long counselorId, DayOfWeek dayOfWeek){
        Counselor counselor = findVerifiedCounselor(counselorId);
        DefaultDay defaultDay = counselor.getDefaultDays().get(dayOfWeek);

        List<LocalTime> times = defaultDay.getDefaultTimeSlots().stream()
                .map(timeslot -> timeslot.getStartTime()).collect(Collectors.toList());

        return times;
    }

    public Counselor findCounselor(long counselorId){
        return findVerifiedCounselor(counselorId);
    }
    public Counselor findCounselor(String userId){
        return findVerifiedCounselor(userId);
    }
    private Counselor findVerifiedCounselor(long counselorId){
        Optional<Counselor> optionalCounselor = counselorRepository.findById(counselorId);
        return optionalCounselor.orElseThrow(() -> new BusinessLogicException(ExceptionCode.COUNSELOR_NOT_FOUND));
    }
    private Counselor findVerifiedCounselor(String userId){
        Optional<Counselor> optionalCounselor = counselorRepository.findByUserId(userId);
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
        if(counselor.getChatPrice() != 0) realCounselor.setChatPrice(counselor.getChatPrice());
        if(counselor.getCallPrice() != 0) realCounselor.setCallPrice(counselor.getCallPrice());
        /*Optional.ofNullable(counselor.getChatPrice())
                        .ifPresent(chatprice -> realCounselor.setChatPrice(chatprice));
        Optional.ofNullable(counselor.getCallPrice())
                        .ifPresent(callprice -> realCounselor.setCallPrice(callprice));*/
        Optional.ofNullable(counselor.getProfileImage())
                        .ifPresent(profileImage -> realCounselor.setProfileImage(profileImage));
        Optional.ofNullable(counselor.getIntroduction())
                        .ifPresent(introduction -> realCounselor.setIntroduction(introduction));
        Optional.ofNullable(counselor.getExpertise())
                        .ifPresent(expertise -> realCounselor.setExpertise(expertise));
        Optional.ofNullable(counselor.getSessionDescription())
                        .ifPresent(sessionDescription -> realCounselor.setSessionDescription(sessionDescription));

        realCounselor.setModifiedAt(LocalDateTime.now());
        return counselorRepository.save(realCounselor);
    }
    private boolean isUserIdAvailable(String userId){
        Optional<Member> optionalMember = memberRepository.findByUserId(userId);
        Optional<Counselor> optionalCounselor = counselorRepository.findByUserId(userId);
        return optionalMember.isEmpty() && optionalCounselor.isEmpty();
    }
    // 전체 상담사 조회
    public List<Counselor> getAllActiveCounselors(){
        List<Counselor> counselors = counselorRepository.findAll();
        return counselors.stream()
                .filter(counselor -> counselor.getCounselorStatus().equals(Counselor.Status.ACTIVE))
                .collect(Collectors.toList());
    }

    // 검증후 Fcm토큰을 저장하는 메서드
    @Transactional
    public void updateFcmToken(long counselorId, String fcmToken) {
        Counselor counselor = counselorRepository.findById(counselorId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.COUNSELOR_NOT_FOUND));
        counselor.setFcmToken(fcmToken);
        counselorRepository.save(counselor);
    }

    // 사용자의 로그인Id를 사용해 사용자 memberId를 조회하는 메서드
    public long getCounselorIdByUserId(String username) {
        return counselorRepository.findByUserId(username)
                .map(Counselor::getCounselorId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    // 남은 상담 건수 조회
    public int getReservationCount(long counselorId){
        List<Reservation> reservations = reservationRepository.findByCounselorId(counselorId);

        // 상태가 PENDING인 상담 건들의 갯수를 반환
        return (int) reservations.stream()
                .filter(reservation -> reservation.getReservationStatus().equals(Reservation.ReservationStatus.PENDING))
                .count();
    }

    // 상담사 탈퇴
    public void quitCounselor(long counselorId){
        // 상담사 가져오기
        Counselor counselor = findVerifiedCounselor(counselorId);

        // 먼저 남은 상담들 가져오기
        List<Reservation> reservations = reservationRepository.findByCounselorId(counselorId).stream()
                .filter(reservation -> reservation.getReservationStatus().equals(Reservation.ReservationStatus.PENDING))
                .collect(Collectors.toList());
        // 상담 예약 취소
        reservations.forEach(reservation -> {
            reservationService.cancelReservationByCounselor(reservation.getReservationId(), "상담사 회원 탈퇴로 자동 취소되었습니다.");
        });
        // 회원 탈퇴
        counselor.setCounselorStatus(Counselor.Status.INACTIVE);
        counselorRepository.save(counselor);
    }
}
