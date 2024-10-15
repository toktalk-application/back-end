package com.springboot.reservation.service;

import com.springboot.auth.CustomAuthenticationToken;
import com.springboot.auth.dto.LoginDto;
import com.springboot.counselor.entity.Counselor;
import com.springboot.counselor.repository.CounselorRepository;
import com.springboot.counselor.service.CounselorService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.reservation.dto.ReservationDto;
import com.springboot.reservation.entity.Report;
import com.springboot.reservation.entity.Reservation;
import com.springboot.reservation.entity.Review;
import com.springboot.reservation.repository.ReservationRepository;
import com.springboot.utils.CalendarUtil;
import com.springboot.utils.CredentialUtil;
import com.springboot.utils.TimeUtils;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final CounselorService counselorService;
    private final MemberService memberService;
    private final CounselorRepository counselorRepository;

    // 상담 예약 등록
    public Reservation createReservation(Reservation reservation, LocalDate date, List<LocalTime> startTimes){
        Counselor counselor = counselorService.findCounselor(reservation.getCounselorId());
        // 최소 한 타임은 예약해야 함
        if(startTimes.isEmpty())throw new BusinessLogicException(ExceptionCode.TIMESLOT_REQUIRED);
        // 예약 시간 정렬
        Collections.sort(startTimes);
        // 이미 지난 시간이면 안됨
        LocalDateTime dateTime = LocalDateTime.of(date, startTimes.get(0));
        if(dateTime.isBefore(LocalDateTime.now())) throw new BusinessLogicException(ExceptionCode.UNAVAILABLE_DATE);
        // 예약 불가능한 날짜
        if(!counselor.getAvailableDates().containsKey(date)) throw new BusinessLogicException(ExceptionCode.UNAVAILABLE_DATE);
        // 예약 가능한 시간인지 검사 (예외는 내부적으로 처리, 예약 시간 등록도 내부적으로 처리)
        counselor.getAvailableDate(date).validateReservationTimes(reservation, startTimes);
        // 예약 시간들이 연속적인지 검사
        Collections.sort(startTimes);
        int firstHour = startTimes.get(0).getHour();
        for(int i = 1; i< startTimes.size(); i++){
            // 첫 예약 타임이 9시라면 그 다음 타임은 10시, 다다음은 11시여야 함. 아니라면 예외 반환
            if(startTimes.get(i).getHour() != firstHour + i) throw new BusinessLogicException(ExceptionCode.DISCONTINUOUS_TIME);
        }
        // ㅡㅡㅡ 시간과 관련된 정보들 비정규화 ㅡㅡㅡ (예약 취소시 관련 정보가 지워지면서 조회가 불가능해지는 문제 해결)
        // 예약일 비정규화
        reservation.setDate(date);
        // 시작시간 ~ 끝시간 비정규화
        reservation.setStartTime(startTimes.get(0));
        reservation.setEndTime(startTimes.get(startTimes.size() - 1).plusMinutes(50));
        return reservationRepository.save(reservation);
    }
    // 리뷰 등록
    public void registerReview(long reservationId, Review review, Authentication authentication){
        // Member만 리뷰 작성 가능
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;
        if(auth.getUserType() != LoginDto.UserType.MEMBER) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        // 진짜로 상담 받은 회원인지 검사
        Reservation reservation = findVerifiedReservation(reservationId);
        long memberId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "memberId"));

        // 다른 놈인데?
        if(reservation.getMember().getMemberId() != memberId) throw new BusinessLogicException(ExceptionCode.UNMATCHED_MEMBER);

        // 상담이 끝난 상태여야만 리뷰 작성 가능
        if(reservation.getReservationStatus() != Reservation.ReservationStatus.COMPLETED) throw new BusinessLogicException(ExceptionCode.UNCOMPLETE_COUNSELING);

        // 이미 리뷰 있어도 안됨
        if(reservation.getReview() != null) throw new BusinessLogicException(ExceptionCode.REVIEW_EXIST);

        // 문제 없으면 리뷰 등록
        reservation.setReview(review); // Reservation <-> Review 양방향 set 메서드
        reservationRepository.save(reservation);

        // 상담사 평점 업데이트
        long counselorId = reservation.getCounselorId();
        Counselor counselor = counselorService.findCounselor(counselorId);
        counselor.updateRating(review.getRating());
        counselorRepository.save(counselor);
    }
    // 상담사 진단 등록
    public void registerReport(long reservationId, Report report, Authentication authentication){
        // Counselor만 진단 등록 가능
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;
        if(auth.getUserType() != LoginDto.UserType.COUNSELOR) throw new BusinessLogicException(ExceptionCode.INVALID_USERTYPE);

        // 진짜로 상담했던 상담사인지 검사
        Reservation reservation = findVerifiedReservation(reservationId);
        long counselorId = Long.parseLong(CredentialUtil.getCredentialField(authentication, "counselorId"));

        // 다른 놈인데?
        if(reservation.getCounselorId() != counselorId) throw new BusinessLogicException(ExceptionCode.UNMATCHED_COUNSELOR);

        // 상담이 끝난 상태여야만 진단 가능
        if(reservation.getReservationStatus() != Reservation.ReservationStatus.COMPLETED) throw new BusinessLogicException(ExceptionCode.UNCOMPLETE_COUNSELING);

        // 이미 진단이 있어도 안됨
        if(reservation.getReport() != null) throw new BusinessLogicException(ExceptionCode.REPORT_EXIST);

        // 문제 없으면 진단 등록
        reservation.setReport(report); // Reservation <-> Report 양방향 set 메서드
        reservationRepository.save(reservation);
    }

    // 특정 회원이 특정 날짜에 잡은 예약 목록 조회
    public List<Reservation> getDailyReservationsByMember(long memberId, LocalDate date, boolean exceptCancelledReservation){
        Member member = memberService.findMember(memberId);
        List<Reservation> reservations = reservationRepository.findByMember(member);

        return reservations.stream()
                .filter(reservation -> {
                    boolean dateMatched = reservation.getDate().equals(date);

                    // exceptCancelledReservation이 false면 그냥 통과, true일 경우 취소되지 않은 예약만 통과
                    return dateMatched && (!exceptCancelledReservation || !reservation.isCancelled());
                })
                .sorted(Comparator.comparing(Reservation::getStartTime)) // 시간 기준으로 오름차순 정렬
                .collect(Collectors.toList());
    }

    // 특정 회원의 한 달간 각 날짜별로, 예약을 잡은 날인지 여부 조회
    public Map<LocalDate, Boolean> getMonthlyReservationsByMember(long memberId, YearMonth month){
        Member member = memberService.findMember(memberId);

        List<Reservation> reservations = reservationRepository.findByMember(member).stream()
                .filter(reservation -> {
                    LocalDate date = reservation.getDate();
                    return CalendarUtil.isLocalDateInYearMonth(date, month);
                }).collect(Collectors.toList());

        // 각 날짜별로 취소되지 않은 예약이 있는지 알아보기
        Map<LocalDate, Boolean> monthlyReservations = new HashMap<>();
        reservations.forEach(reservation -> {
            LocalDate reservationDate = reservation.getDate();
            if(!monthlyReservations.containsKey(reservationDate) && !reservation.isCancelled()){
                monthlyReservations.put(reservationDate, true);
            }
        });
        return monthlyReservations;
    }

    // 특정 회원이 특정월에 예약한 모든 상담 조회
    public List<Reservation> getDetailedMonthlyReservations(long memberId, YearMonth month){
        Member member = memberService.findMember(memberId);

        // 멤버의 전체 예약 가져오기
        List<Reservation> reservations = reservationRepository.findByMember(member);
        // 해당월에 잡힌 예약만 반환
        return reservations.stream()
                .filter(reservation -> CalendarUtil.isLocalDateInYearMonth(reservation.getDate(), month))
                .sorted(Comparator.comparing(Reservation::getDate))// 날짜순 정렬
                .sorted(Comparator.comparing(Reservation::getStartTime))// 그 다음 시간순 정렬
                .collect(Collectors.toList());
    }

    // 특정 상담사의 특정 날짜에 잡힌 예약 목록 조회
    public List<Reservation> getDailyReservationsWithCounselor(long counselorId, LocalDate date){
        Counselor counselor = counselorService.findCounselor(counselorId);

        Set<Reservation> reservations = new HashSet<>();
        counselor.getAvailableDate(date).getAvailableTimes().values().forEach(time -> {
            if(time.getReservation() != null){
                reservations.add(time.getReservation());
            }
        });
        return new ArrayList<>(reservations).stream()
                .sorted(Comparator.comparing(Reservation::getStartTime))
                .collect(Collectors.toList());
    }

    // 특정 상담사의 한 달간 각 날짜별로, 예약이 있는 날인지 여부 조회
    public Map<LocalDate, Boolean> getMonthlyReservationsWithCounselor(long counselorId, YearMonth month){
        Counselor counselor = counselorService.findCounselor(counselorId);

        // 해당 월의 날짜들 구하기
        List<LocalDate> dates = CalendarUtil.getMonthDates(month);

        Map<LocalDate, Boolean> result = new HashMap<>();
        // 각 날짜별로 예약이 있는지 알아보기
        dates.forEach(date -> {
            Boolean isReserved;
            try{
                isReserved = counselor.getAvailableDate(date).isReservedDate();
            } catch (BusinessLogicException e){
                isReserved = null; // true, false 가 아니라 아예 예약 불가능한 날짜라는 뜻
            }
            result.put(date, isReserved);
        });
        return result;
    }

    // 특정 상담사에게 특정월에 잡힌 모든 예약 정보 조회
    public List<Reservation> getMonthlyDetailReservations(long counselorId, YearMonth month){
        // 특정 상담사의 예약 가져오기
        List<Reservation> reservations = reservationRepository.findByCounselorId(counselorId);

        // 해당월의 건만 반환
        return reservations.stream()
                .filter(reservation -> CalendarUtil.isLocalDateInYearMonth(reservation.getDate(), month))
                .sorted(Comparator.comparing(Reservation::getDate) // 날짜순으로 정렬
                        .thenComparing(Reservation::getStartTime)) // 그다음 시간순 정렬
                .collect(Collectors.toList());
    }

    public Reservation findReservation(long reservationId){
        return findVerifiedReservation(reservationId);
    }

    private Reservation findVerifiedReservation(long reservationId){
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservationId);
        return optionalReservation.orElseThrow(() -> new BusinessLogicException(ExceptionCode.RESERVATION_NOT_FOUND));
    }

    // 예약 취소 (MEMBER)
    public void cancelReservationByMember(long reservationId){
        // 예약 정보 찾기
        Reservation reservation = findReservation(reservationId);
        // 취소는 최소 24시간 전
        LocalTime startTime = reservation.getReservationTimePeriod().getStartTime();
        LocalDate reservationDate = reservation.getDate();
        LocalDateTime startDateTime = LocalDateTime.of(reservationDate, startTime);
        Duration duration = Duration.between(LocalDateTime.now(), startDateTime);
        if(duration.toHours() < 24) throw new BusinessLogicException(ExceptionCode.CANCELLATION_TOO_LATE);
        // 상담사 밑으로 잡혀있는 예약 정보 없애기
        reservation.getReservationTimes().forEach(time -> {
            time.setReservation(null);
        });
        // 예약 상태 바꾸기
        reservation.setReservationStatus(Reservation.ReservationStatus.CANCELLED_BY_CLIENT);

        // 바뀐 상태 저장하고 리턴
        reservationRepository.save(reservation);
    }

    // 예약 취소 (COUNSELOR)
    public void cancelReservationByCounselor(long reservationId, String cancelReason){
        // 예약 정보 찾기
        Reservation reservation = findReservation(reservationId);
        // 취소는 최소 24시간 전
        LocalTime startTime = reservation.getReservationTimePeriod().getStartTime();
        LocalDate reservationDate = reservation.getDate();
        LocalDateTime startDateTime = LocalDateTime.of(reservationDate, startTime);
        Duration duration = Duration.between(LocalDateTime.now(), startDateTime);
        if(duration.toHours() < 24) throw new BusinessLogicException(ExceptionCode.CANCELLATION_TOO_LATE);
        // 상담사 밑으로 잡혀있는 예약 정보 없애기
        reservation.getReservationTimes().forEach(time -> {
            time.setReservation(null);
        });
        // 예약 상태 바꾸기
        reservation.setReservationStatus(Reservation.ReservationStatus.CANCELLED_BY_COUNSELOR);
        // 취소 사유 등록
        reservation.setCancelComment(cancelReason);
        /*switch (cancelReason){
            case 1:
                reservation.setCancelComment("ㅈㅅ");
                break;
            case 2:
                reservation.setCancelComment("ㅈㅅ ㅋ");
                break;
            case 3:
                reservation.setCancelComment("ㅈㅅ ㅋㅋ");
                break;
            default:
                throw new BusinessLogicException(ExceptionCode.INVALID_CANCLE_REASON);
        }*/
        // 바뀐 상태 저장하고 리턴
        reservationRepository.save(reservation);
    }

    // 상담의 상태를 완료로 변경
    /*@Scheduled(cron = "0 50 * * * ?") // 매 시 50분마다 실행
    private void completeCounselling(){
        // 상태가 PENDING인 상담들 조회
        List<Reservation> pendingReservations = reservationRepository.findByReservationStatus(Reservation.ReservationStatus.PENDING);
        // 상담들 순회하며 조건 확인
        pendingReservations.forEach(reservation -> {
            LocalDateTime endTime = LocalDateTime.of(reservation.getDate(), reservation.getEndTime());
            // 종료 시간이 되었다면 상태를 완료로 변경
            if(TimeUtils.isPassedTime(endTime)) reservation.setReservationStatus(Reservation.ReservationStatus.COMPLETED);
            reservationRepository.save(reservation);
        });
    }*/

    // 테스트용 스케줄러
    @Scheduled(cron = "0 */1 * * * *")
    private void testScheduler(){
        List<Reservation> pendingReservations = reservationRepository.findByReservationStatus(Reservation.ReservationStatus.PENDING);
        pendingReservations.forEach(reservation -> {
            // 그냥 완료로 변경
            reservation.setReservationStatus(Reservation.ReservationStatus.COMPLETED);
            reservationRepository.save(reservation);
            ;
        });
    }
}
