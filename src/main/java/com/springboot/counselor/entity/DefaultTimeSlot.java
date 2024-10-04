package com.springboot.counselor.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultTimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long defaultTimeSlotId;

    @ManyToOne
    @JoinColumn(name = "defaultDayId")
    private DefaultDay defaultDay;

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime endTime;

    public void setDefaultDay(DefaultDay defaultDay){
        this.defaultDay = defaultDay;
        if(!defaultDay.getDefaultTimeSlots().contains(this)){
            defaultDay.addDefaultTimeSlot(this);
        }
    }
}
