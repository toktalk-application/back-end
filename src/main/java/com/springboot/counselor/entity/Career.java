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
public class Career {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long careerId;

    @ManyToOne
    @JoinColumn(name = "counselor_id")
    private Counselor counselor;

    @Enumerated(EnumType.STRING)
    private Classification classification;

    @Column
    private String company;

    @Column
    private String responsibility;

    public enum Classification{
        CURRENT,
        PREVIOUS
    }

    public void setCounselor(Counselor counselor){
        this.counselor = counselor;
        if(!counselor.getCareers().contains(this)){
            counselor.addCareer(this);
        }
    }
}
