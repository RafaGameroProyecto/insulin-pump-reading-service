package com.insulinpump.readingservice.dto;

import com.insulinpump.readingservice.model.ReadingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadingCreateDto {

    @NotNull(message = "El nivel de glucosa es obligatorio")
    @Positive(message = "El nivel de glucosa debe ser un valor positivo")
    private Float glucoseLevel;

    private LocalDateTime timestamp;

    @NotNull(message = "El ID del dispositivo es obligatorio")
    private Long deviceId;

    private ReadingStatus status;
    private String notes;
    private Float insulinDose;
    private Float carbIntake;
    private Boolean manualReading;
    private Boolean requiresAction;
}
