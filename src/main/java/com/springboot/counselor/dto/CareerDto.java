package com.springboot.counselor.dto;

import com.springboot.counselor.entity.Career;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CareerDto {
    private long careerId;
    private Career.Classification classification;
    private String company;
    private String responsibility;
}
