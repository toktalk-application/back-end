package com.springboot.testresult.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long testResultId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @JsonBackReference("member-testresult")
    private Member member;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TestLevel testLevel = TestLevel.NORMAL;

    @Column(nullable = false, length = 255)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TestLevel {
        NORMAL("정상"),
        MILD("경미한수준"),
        MODERATE("중간수준"),
        MODERATELY_SEVERE("약간심한수준"),
        SEVERE("심한수준");

        @Getter
        @Setter
        private String description;

        TestLevel(String description) {
            this.description = description;
        }
    }
}
