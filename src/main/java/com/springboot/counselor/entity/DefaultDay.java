package com.springboot.counselor.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long defaultDayId;

    @ManyToOne
    @JoinColumn(name = "counselorId")
    private Counselor counselor;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @OneToMany(mappedBy = "defaultDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DefaultTimeSlot> defaultTimeSlots = new ArrayList<>();

    public void setCounselor(Counselor counselor){
        this.counselor = counselor;
        if(!counselor.getDefaultDays().containsValue(this)){
            counselor.addDefaultDay(this);
        }
    }

    public void addDefaultTimeSlot(DefaultTimeSlot defaultTimeSlot){
        defaultTimeSlots.add(defaultTimeSlot);
        if(defaultTimeSlot.getDefaultDay() == null){
            defaultTimeSlot.setDefaultDay(this);
        }
    }

    // 이 요일에 속한 시간들을 LocalTime형태로 반환
    public List<LocalTime> getStartTimes(){
        return defaultTimeSlots.stream()
                .map(timeslot -> timeslot.getStartTime())
                .collect(Collectors.toList());
    }
}
