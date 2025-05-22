package com.insulinpump.readingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {
    private Long id;
    private String name;
    private Integer age;
    private String medicalId;
    private Long deviceId;
    private String diabetesType;
}
