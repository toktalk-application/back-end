package com.springboot.reservation.entity;

import com.springboot.counselor.available_date.AvailableTime;
import com.springboot.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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
        // 예약 타임들이 정렬이 안 되어 있다면
        /*LocalTime startTime = LocalTime.MAX;
        LocalTime endTime = LocalTime.MIN;

        // startTime중 가장 이른 시점과 endTime중 가장 나중 시점을 뽑기
        for(AvailableTime time : reservationTimes){
            startTime = startTime.isBefore(time.getStartTime()) ? startTime : time.getStartTime();
            endTime = endTime.isAfter(time.getEndTime()) ? endTime : time.getEndTime();
        }
        return new TimePeriod(startTime, endTime);*/
        // 이미 예약 타임들이 정렬되어 DB에 들어가 있다면
        return new TimePeriod(reservationTimes.get(0).getStartTime(), reservationTimes.get(reservationTimes.size() - 1).getEndTime());
    }
    @Getter
    @Setter
    @AllArgsConstructor
    public class TimePeriod{
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
