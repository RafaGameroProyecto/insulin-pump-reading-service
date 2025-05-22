package com.insulinpump.readingservice.dto;

import com.insulinpump.readingservice.model.Reading;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ReadingDetailsDto {
    private Long id;
    private Float glucoseLevel;
    private LocalDateTime timestamp;
    private Long deviceId;
    private String status;
    private String notes;
    private Float insulinDose;
    private Float carbIntake;
    private Boolean manualReading;
    private Boolean requiresAction;
    private DeviceDto device;
    private PatientDto patient;

    public ReadingDetailsDto(Reading reading) {
        this.id = reading.getId();
        this.glucoseLevel = reading.getGlucoseLevel();
        this.timestamp = reading.getTimestamp();
        this.deviceId = reading.getDeviceId();
        this.status = reading.getStatus() != null ? reading.getStatus().toString() : null;
        this.notes = reading.getNotes();
        this.insulinDose = reading.getInsulinDose();
        this.carbIntake = reading.getCarbIntake();
        this.manualReading = reading.getManualReading();
        this.requiresAction = reading.getRequiresAction();
    }
}
