package com.springboot.member.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.springboot.chat.entity.ChatRoom;
import com.springboot.gender.Gender;
import com.springboot.reservation.entity.Reservation;
import com.springboot.testresult.entity.TestResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long memberId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column
    private LocalDate birth;

    @Column
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Map<LocalDate, DailyMood> dailyMoods = new HashMap<>();

    @OneToMany(mappedBy = "member")
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    @JsonManagedReference("member-testresult")
    private List<TestResult> testResults = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    @JsonManagedReference("member-chatroom")
    private List<ChatRoom> chatRooms = new ArrayList<>();

    @Column
    @Enumerated(EnumType.STRING)
    private Status memberStatus = Status.ACTIVE;

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime modifiedAt = LocalDateTime.now();

    @ElementCollection
    private List<String> roles = new ArrayList<>();

    @Column
    private String fcmToken;

    @AllArgsConstructor
    public enum Status {
        ACTIVE,
        INACTIVE
    }
    public void addReservation(Reservation reservation){
        reservations.add(reservation);
        if(reservation.getMember() == null){
            reservation.setMember(this);
        }
    }

    public void addDailyMood(LocalDate date, DailyMood dailyMood){
        dailyMoods.put(date, dailyMood);
        if(dailyMood.getMember() == null) {
            dailyMood.setMember(this);
        };
    }
}
