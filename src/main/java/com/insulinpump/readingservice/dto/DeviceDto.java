package com.insulinpump.readingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDto {
    private Long id;
    private String serialNo;
    private String model;
    private String manufacturer;
    private String status;
    private Long patientId;
}
