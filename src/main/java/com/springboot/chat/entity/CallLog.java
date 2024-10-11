package com.springboot.chat.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CallLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long logId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    @JsonBackReference("chatroom-calllog")
    private ChatRoom chatRoom;

    @Column
    private String fileUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
