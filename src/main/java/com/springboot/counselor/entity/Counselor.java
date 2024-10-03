package com.springboot.counselor.entity;

import com.springboot.counselor.available_date.AvailableDate;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.gender.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Map<LocalDate ,AvailableDate> availableDates = new HashMap();

    @ElementCollection
    private List<String> roles = new ArrayList<>();

    @Column
    private String company;

    @Column
    private int chatPrice = 30000;

    @Column
    private int callPrice = 50000;

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Career> careers = new ArrayList<>();

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<License> licenses = new ArrayList<>();

    @OneToMany(mappedBy = "counselor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Keyword> keywords = new ArrayList<>();

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

    // 날짜를 통해 상담사가 가진 AvailableDate객체 반환
    public AvailableDate getAvailableDate(LocalDate date){
        AvailableDate availableDate = availableDates.get(date);
        if(availableDate == null) throw new BusinessLogicException(ExceptionCode.UNAVAILABLE_DATE);
        return availableDate;
    }
}
