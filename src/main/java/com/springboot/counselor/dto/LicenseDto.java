package com.springboot.counselor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LicenseDto {
    private long licenseDto;
    private String licenseName;
    private String organization;
    private LocalDate issueDate;
}
