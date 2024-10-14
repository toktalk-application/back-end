package com.springboot.reservation.entity;

import com.springboot.counselor.available_date.AvailableTime;
import com.springboot.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long reservationId;

    @Column
    private long counselorId;

    @Column
    private String counselorName;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus = ReservationStatus.PENDING;

    @Column
    private String comment;

    @Column
    private String cancelComment;

    @Column
    private int fee;

    @Column
    private LocalDate date;

    @Column
    @Enumerated(EnumType.STRING)
    private CounselingType type;

    @OneToMany(mappedBy = "reservation")
    private List<AvailableTime> reservationTimes = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "review_id")
    private Review review;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "report_id")
    private Report report;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime modifiedAt = LocalDateTime.now();

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime endTime;

    public enum CounselingType {
        CALL,
        CHAT
    }

    public enum ReservationStatus{
        PENDING,
        CANCELLED_BY_CLIENT,
        CANCELLED_BY_COUNSELOR,
        COMPLETED,
        REPORT_COMPLETED
    }

    public void setMember(Member member){
        this.member = member;
        if(!member.getReservations().contains(this)){
            member.addReservation(this);
        }
    }

    // 예약의 시작 시점과 종료 시점 구하기
    public TimePeriod getReservationTimePeriod(){
        // 이미 예약 타임들이 정렬되어 DB에 들어가 있다면
        /*return new TimePeriod(reservationTimes.get(0).getStartTime(), reservationTimes.get(reservationTimes.size() - 1).getEndTime());*/
        // 예약 취소시 시간을 조회할 수 없기 때문에 그냥 필드 비정규화함
        return new TimePeriod(startTime, endTime);
    }
    @Getter
    @Setter
    @AllArgsConstructor
    public class TimePeriod{
        private LocalTime startTime;
        private LocalTime endTime;
    }

    public boolean isCancelled(){
        return reservationStatus.equals(ReservationStatus.CANCELLED_BY_CLIENT) || reservationStatus.equals(ReservationStatus.CANCELLED_BY_COUNSELOR);
    }
}
