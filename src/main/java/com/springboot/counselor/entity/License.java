package com.springboot.counselor.entity;

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
@AllArgsConstructor
public class License {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long licenseId;

    @ManyToOne
    @JoinColumn(name = "counselerId")
    private Counselor counselor;

    @Column
    private String licenseName;

    @Column
    private String organization;

    public void setCounselor(Counselor counselor){
        this.counselor = counselor;
        if(!counselor.getLicenses().contains(this)){
            counselor.getLicenses().add(this);
        }
    }
}
