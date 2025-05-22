package com.insulinpump.readingservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lecturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El nivel de glucosa es obligatorio")
    @Positive(message = "El nivel de glucosa debe ser un valor positivo")
    private Float glucoseLevel;

    @NotNull(message = "La marca temporal es obligatoria")
    private LocalDateTime timestamp;

    @NotNull(message = "El ID del dispositivo es obligatorio")
    private Long deviceId;

    @Enumerated(EnumType.STRING)
    private ReadingStatus status;

    private String notes;

    // Datos adicionales
    private Float insulinDose;
    private Float carbIntake;
    private Boolean manualReading;
    private Boolean requiresAction;
}

