package com.springboot.chat.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.springboot.counselor.entity.Counselor;
import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long roomId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @JsonBackReference("member-chatroom")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "counselor_id")
    @JsonBackReference("counselor-chatroom")
    private Counselor counselor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RoomStatus roomStatus = RoomStatus.CLOSE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CallStatus callStatus = CallStatus.INACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("chatroom-chatlog")
    private List<ChatLog> chatLogs = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("chatroom-calllog")
    private List<CallLog> callLogs = new ArrayList<>();

    public enum RoomStatus {
        OPEN("open"),
        CLOSE("close");

        @Getter
        @Setter
        private String status;

        RoomStatus(String status) {
            this.status = status;
        }
    }

    public enum CallStatus {
        ACTIVE("통화중"),
        INACTIVE("통화종료"),
        CONNECTING("통화 연결중");

        private String status;

        CallStatus(String status) {
            this.status = status;
        }
    }
}
