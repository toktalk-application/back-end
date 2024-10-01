package com.springboot.counselor.available_date;

import com.springboot.counselor.entity.Counselor;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.reservation.entity.Reservation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AvailableDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long availableDateId;

    @ManyToOne
    @JoinColumn(name = "counselor_id")
    private Counselor counselor;

    @Column
    private LocalDate date;

    @OneToMany(mappedBy = "availableDate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvailableTime> availableTimes = new ArrayList<>();

    public AvailableDate(LocalDate date){
        this.date = date;
        // 9 ~ 24시까지 예약가능시간 자동 생성
        for(int i = 9; i< 24; i++){
            addAvailableTime(new AvailableTime(LocalTime.of(i,0), LocalTime.of(i,50)));
        }
    }

    public void setCounselor(Counselor counselor){
        this.counselor = counselor;
        if(!counselor.getAvailableDates().containsKey(this)){
            counselor.getAvailableDates().put(date, this);
        }
    }
    public void addAvailableTime(AvailableTime availableTime){
        availableTimes.add(availableTime);
        if(availableTime.getAvailableDate() == null){
            availableTime.setAvailableDate(this);
        }
    }
    private void validateReservationTime(Reservation reservation, LocalTime reservationTime){
        for(AvailableTime time : availableTimes){
            if(time.getStartTime().equals(reservationTime)){
                if(time.getReservation() != null){
                    // 이미 예약된 시간이다 임마
                    throw new BusinessLogicException(ExceptionCode.RESERVATION_TIMESLOT_OCCUPIED);
                }else{
                    // 유효한 예약 가능 시간을 발견하면 예약 시간을 등록하고 탈출
                    time.setReservation(reservation);
                    return;
                }
            }
        }
        // 입력한 시간이 예약 가능 시간대에 없을 때
        throw new BusinessLogicException(ExceptionCode.UNAVAILABLE_TIME);
    }
    public void validateReservationTimes(Reservation reservation, List<LocalTime> reservationTimes) {
        for(LocalTime reservationTime: reservationTimes){
            validateReservationTime(reservation, reservationTime);
        }
    }
}
