package com.insulinpump.readingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlucoseStatisticsDto {
    private Long deviceId;
    private String deviceSerialNo;
    private String patientName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Float averageGlucoseLevel;
    private Long lowReadingsCount;
    private Long highReadingsCount;
    private Float lowestReading;
    private Float highestReading;
    private Integer totalReadings;
    private Float standardDeviation;
}
