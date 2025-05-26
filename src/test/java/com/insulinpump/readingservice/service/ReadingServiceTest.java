package com.insulinpump.readingservice.service;

import com.insulinpump.readingservice.client.DeviceClient;
import com.insulinpump.readingservice.client.PatientClient;
import com.insulinpump.readingservice.dto.DeviceDto;
import com.insulinpump.readingservice.dto.ReadingCreateDto;
import com.insulinpump.readingservice.dto.ReadingDetailsDto;
import com.insulinpump.readingservice.exception.ReadingNotFoundException;
import com.insulinpump.readingservice.model.Reading;
import com.insulinpump.readingservice.model.ReadingStatus;
import com.insulinpump.readingservice.repository.ReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingServiceTest {

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private DeviceClient deviceClient;

    @Mock
    private PatientClient patientClient;

    @InjectMocks
    private ReadingService readingService;

    private Reading testReading;
    private ReadingCreateDto testReadingCreateDto;
    private DeviceDto testDevice;

    @BeforeEach
    void setUp() {
        testReading = createTestReading();
        testReadingCreateDto = createTestReadingCreateDto();
        testDevice = createTestDevice();
    }

    @Test
    void should_get_all_readings() {
        // Given
        when(readingRepository.findAll()).thenReturn(Arrays.asList(testReading));

        // When
        List<ReadingDetailsDto> result = readingService.getAllReadings();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGlucoseLevel()).isEqualTo(100.0f);
    }

    @Test
    void should_get_reading_by_id() {
        // Given
        when(readingRepository.findById(1L)).thenReturn(Optional.of(testReading));

        // When
        ReadingDetailsDto result = readingService.getReadingById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getGlucoseLevel()).isEqualTo(100.0f);
    }

    @Test
    void should_throw_exception_when_reading_not_found() {
        // Given
        when(readingRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> readingService.getReadingById(1L))
                .isInstanceOf(ReadingNotFoundException.class);
    }

    @Test
    @Transactional
    void should_create_reading() {
        // Given
        when(deviceClient.getDeviceById(1L)).thenReturn(testDevice);
        when(readingRepository.save(any(Reading.class))).thenReturn(testReading);

        // When
        ReadingDetailsDto result = readingService.createReading(testReadingCreateDto);

        // Then
        assertThat(result.getGlucoseLevel()).isEqualTo(100.0f);
        verify(deviceClient).getDeviceById(1L);
        verify(readingRepository).save(any(Reading.class));
    }

    @Test
    @Transactional
    void should_delete_reading() {
        // Given
        when(readingRepository.findById(1L)).thenReturn(Optional.of(testReading));

        // When
        readingService.deleteReading(1L);

        // Then
        verify(readingRepository).delete(testReading);
    }

    private Reading createTestReading() {
        Reading reading = new Reading();
        reading.setId(1L);
        reading.setGlucoseLevel(100.0f);
        reading.setDeviceId(1L);
        reading.setStatus(ReadingStatus.NORMAL);
        reading.setTimestamp(LocalDateTime.now());
        reading.setManualReading(false);
        reading.setRequiresAction(false);
        return reading;
    }

    private ReadingCreateDto createTestReadingCreateDto() {
        ReadingCreateDto dto = new ReadingCreateDto();
        dto.setGlucoseLevel(100.0f);
        dto.setDeviceId(1L);
        dto.setTimestamp(LocalDateTime.now());
        return dto;
    }

    private DeviceDto createTestDevice() {
        DeviceDto device = new DeviceDto();
        device.setId(1L);
        device.setSerialNo("DEV123");
        device.setModel("Model X");
        return device;
    }
}
