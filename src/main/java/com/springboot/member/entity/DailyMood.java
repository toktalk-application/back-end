package com.springboot.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DailyMood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long moodId;

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member member;

    @Enumerated(EnumType.STRING)
    private Mood mood;

    @Column
    private LocalDate date;

    public enum Mood{
        ANGRY,
        HAPPY,
        NEUTRAL,
        SAD,
        DEPRESSED
    }

    public void setMember(Member member){
        this.member = member;
        if(!member.getDailyMoods().containsKey(date)) {
            member.addDailyMood(date, this);
        };
    }
}
