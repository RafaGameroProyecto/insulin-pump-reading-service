package com.insulinpump.readingservice.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void should_validate_reading_successfully() {
        // Given
        Reading reading = createValidReading();

        // When
        Set<ConstraintViolation<Reading>> violations = validator.validate(reading);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void should_fail_validation_when_glucose_level_is_null() {
        // Given
        Reading reading = createValidReading();
        reading.setGlucoseLevel(null);

        // When
        Set<ConstraintViolation<Reading>> violations = validator.validate(reading);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("El nivel de glucosa es obligatorio");
    }

    @Test
    void should_fail_validation_when_device_id_is_null() {
        // Given
        Reading reading = createValidReading();
        reading.setDeviceId(null);

        // When
        Set<ConstraintViolation<Reading>> violations = validator.validate(reading);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("El ID del dispositivo es obligatorio");
    }

    private Reading createValidReading() {
        Reading reading = new Reading();
        reading.setGlucoseLevel(100.0f);
        reading.setDeviceId(1L);
        reading.setTimestamp(LocalDateTime.now());
        reading.setStatus(ReadingStatus.NORMAL);
        return reading;
    }
}