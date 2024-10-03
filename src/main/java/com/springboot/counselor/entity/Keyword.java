package com.springboot.counselor.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long keywordId;

    @Column
    private String word;

    @ManyToOne
    @JoinColumn(name = "counselorId")
    private Counselor counselor;

    public void setCounselor(Counselor counselor){
        this.counselor = counselor;
        if(!counselor.getKeywords().contains(this)){
            counselor.getKeywords().add(this);
        }
    }
}
