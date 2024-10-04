package com.springboot.counselor.available_date;

import com.springboot.reservation.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Time;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailableTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long availableTimeId;

    @ManyToOne
    @JoinColumn(name = "available_date_id")
    private AvailableDate availableDate;

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    public AvailableTime(LocalTime startTime, LocalTime endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void setAvailableDate(AvailableDate availableDate){
        this.availableDate = availableDate;
        if(!availableDate.getAvailableTimes().containsValue(this)){
            availableDate.addAvailableTime(this);
        }
    }
}
