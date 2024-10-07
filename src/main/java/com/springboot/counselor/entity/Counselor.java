package com.springboot.counselor.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.springboot.chat.entity.ChatRoom;
import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.gender.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Counselor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long counselorId;

    @Column
    private String password;

    @Column
    private String phone;

    @Column
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Status counselorStatus = Status.VERIFICATION_WAITING;

    @Column
    private String ci;

    @Column
    private String name;

    @Column
    private String userId;

    @ElementCollection
    private List<String> roles = new ArrayList<>();

    @Column
    private String company;

    @Column
    private int chatPrice = 30000;

    @Column
    private int callPrice = 50000;

    @Column
    private String profileImage;

    @Column
    private String introduction;

    @Column
    private String expertise;

    @Column
    private String sessionDescription;

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Map<LocalDate, AvailableDate> availableDates = new HashMap();

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Career> careers = new ArrayList<>();

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<License> licenses = new ArrayList<>();

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Keyword> keywords = new ArrayList<>();

    @OneToMany(mappedBy = "counselor")
    @JsonManagedReference("counselor-chatroom")
    private List<ChatRoom> chatRooms = new ArrayList<>();

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Map<DayOfWeek, DefaultDay> defaultDays = new HashMap<>();

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime modifiedAt = LocalDateTime.now();

    public enum Status {
        VERIFICATION_WAITING,
        ACTIVE,
        INACTIVE
    }

    public void addAvailableDate(AvailableDate availableDate){
        availableDates.put(availableDate.getDate(), availableDate);
        if(availableDate.getCounselor() == null){
            availableDate.setCounselor(this);
        }
    }

    public void addCareer(Career career){
        careers.add(career);
        if(career.getCounselor() == null){
            career.setCounselor(this);
        }
    }

    public void addLicense(License license){
        licenses.add(license);
        if(license.getCounselor() == null){
            license.setCounselor(this);
        }
    }

    public void addKeyword(Keyword keyword){
        keywords.add(keyword);
        if(keyword.getCounselor() == null){
            keyword.setCounselor(this);
        }
    }

    public void addDefaultDay(DefaultDay defaultDay){
        defaultDays.put(defaultDay.getDayOfWeek(), defaultDay);
        if(defaultDay.getCounselor() == null){
            defaultDay.setCounselor(this);
        }
    }

    // 날짜를 통해 상담사가 가진 AvailableDate객체 반환
    public AvailableDate getAvailableDate(LocalDate date){
        AvailableDate availableDate = availableDates.get(date);
        if(availableDate == null) throw new BusinessLogicException(ExceptionCode.UNAVAILABLE_DATE);
        return availableDate;
    }

    // 특정 요일에 해당하는 AvailableDate 반환
    public List<AvailableDate> getAvailableDatesInCertainDayOfWeek(DayOfWeek dayOfWeek){
        return availableDates.entrySet().stream()
                .filter(entry -> entry.getKey().getDayOfWeek().equals(dayOfWeek))
                .map(entry -> entry.getValue())
                .collect(Collectors.toList());
    }
}
