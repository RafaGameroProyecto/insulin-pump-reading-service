package com.insulinpump.readingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.insulinpump.readingservice.dto.GlucoseStatisticsDto;
import com.insulinpump.readingservice.dto.ReadingCreateDto;
import com.insulinpump.readingservice.dto.ReadingDetailsDto;
import com.insulinpump.readingservice.exception.GlobalExceptionHandler;
import com.insulinpump.readingservice.exception.ReadingNotFoundException;
import com.insulinpump.readingservice.model.ReadingStatus;
import com.insulinpump.readingservice.service.ReadingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReadingControllerTest {

    @Mock
    private ReadingService readingService;

    @InjectMocks
    private ReadingController readingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Configurar MockMvc con manejo de excepciones
        mockMvc = MockMvcBuilders
                .standaloneSetup(readingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Configurar ObjectMapper para manejar LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void should_get_all_readings() throws Exception {
        // Given
        List<ReadingDetailsDto> readings = Arrays.asList(createTestDto());
        when(readingService.getAllReadings()).thenReturn(readings);

        // When & Then
        mockMvc.perform(get("/api/readings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].glucoseLevel").value(100.0))
                .andExpect(jsonPath("$[0].status").value("NORMAL"));

        verify(readingService, times(1)).getAllReadings();
    }

    @Test
    void should_get_reading_by_id() throws Exception {
        // Given
        when(readingService.getReadingById(1L)).thenReturn(createTestDto());

        // When & Then
        mockMvc.perform(get("/api/readings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.glucoseLevel").value(100.0))
                .andExpect(jsonPath("$.deviceId").value(1));

        verify(readingService, times(1)).getReadingById(1L);
    }

    @Test
    void should_return_404_when_reading_not_found() throws Exception {
        // Given
        when(readingService.getReadingById(999L))
                .thenThrow(new ReadingNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/readings/999"))
                .andExpect(status().isNotFound());

        verify(readingService, times(1)).getReadingById(999L);
    }

    @Test
    void should_get_readings_by_device_id() throws Exception {
        // Given
        List<ReadingDetailsDto> readings = Arrays.asList(createTestDto());
        when(readingService.getReadingsByDeviceId(1L)).thenReturn(readings);

        // When & Then
        mockMvc.perform(get("/api/readings/device/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value(1));

        verify(readingService, times(1)).getReadingsByDeviceId(1L);
    }

    @Test
    void should_get_readings_by_patient_id() throws Exception {
        // Given
        List<ReadingDetailsDto> readings = Arrays.asList(createTestDto());
        when(readingService.getReadingsByPatientId(100L)).thenReturn(readings);

        // When & Then
        mockMvc.perform(get("/api/readings/patient/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].glucoseLevel").value(100.0));

        verify(readingService, times(1)).getReadingsByPatientId(100L);
    }

    @Test
    void should_get_latest_reading_by_device() throws Exception {
        // Given
        when(readingService.getLatestReadingByDeviceId(1L)).thenReturn(createTestDto());

        // When & Then
        mockMvc.perform(get("/api/readings/device/1/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.glucoseLevel").value(100.0));

        verify(readingService, times(1)).getLatestReadingByDeviceId(1L);
    }

    @Test
    void should_get_readings_by_time_range() throws Exception {
        // Given
        List<ReadingDetailsDto> readings = Arrays.asList(createTestDto());
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();

        when(readingService.getReadingsByDeviceIdAndTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(readings);

        // When & Then
        mockMvc.perform(get("/api/readings/device/1/timerange")
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].glucoseLevel").value(100.0));

        verify(readingService, times(1)).getReadingsByDeviceIdAndTimeRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void should_get_glucose_statistics() throws Exception {
        // Given
        GlucoseStatisticsDto stats = createTestStatistics();
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();

        when(readingService.getGlucoseStatisticsByDeviceId(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/readings/device/1/statistics")
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageGlucoseLevel").value(110.5))
                .andExpect(jsonPath("$.totalReadings").value(10));

        verify(readingService, times(1)).getGlucoseStatisticsByDeviceId(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void should_get_readings_by_status() throws Exception {
        // Given
        List<ReadingDetailsDto> readings = Arrays.asList(createTestDto());
        when(readingService.getReadingsByStatus(ReadingStatus.NORMAL)).thenReturn(readings);

        // When & Then
        mockMvc.perform(get("/api/readings/status/NORMAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("NORMAL"));

        verify(readingService, times(1)).getReadingsByStatus(ReadingStatus.NORMAL);
    }

    @Test
    void should_get_readings_requiring_action() throws Exception {
        // Given
        ReadingDetailsDto criticalReading = createTestDto();
        criticalReading.setRequiresAction(true);
        criticalReading.setStatus("CRITICAL_HIGH");

        when(readingService.getReadingsRequiringAction()).thenReturn(Arrays.asList(criticalReading));

        // When & Then
        mockMvc.perform(get("/api/readings/requiring-action"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requiresAction").value(true))
                .andExpect(jsonPath("$[0].status").value("CRITICAL_HIGH"));

        verify(readingService, times(1)).getReadingsRequiringAction();
    }

    @Test
    void should_create_reading() throws Exception {
        // Given
        ReadingCreateDto createDto = createTestCreateDto();
        when(readingService.createReading(any(ReadingCreateDto.class))).thenReturn(createTestDto());

        // When & Then
        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.glucoseLevel").value(100.0))
                .andExpect(jsonPath("$.deviceId").value(1));

        verify(readingService, times(1)).createReading(any(ReadingCreateDto.class));
    }

    @Test
    void should_update_reading() throws Exception {
        // Given
        ReadingCreateDto updateDto = createTestCreateDto();
        updateDto.setGlucoseLevel(120.0f);

        ReadingDetailsDto updatedReading = createTestDto();
        updatedReading.setGlucoseLevel(120.0f);

        when(readingService.updateReading(eq(1L), any(ReadingCreateDto.class))).thenReturn(updatedReading);

        // When & Then
        mockMvc.perform(put("/api/readings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.glucoseLevel").value(120.0));

        verify(readingService, times(1)).updateReading(eq(1L), any(ReadingCreateDto.class));
    }

    @Test
    void should_delete_reading() throws Exception {
        // Given
        doNothing().when(readingService).deleteReading(1L);

        // When & Then
        mockMvc.perform(delete("/api/readings/1"))
                .andExpect(status().isNoContent());

        verify(readingService, times(1)).deleteReading(1L);
    }

    @Test
    void should_return_400_for_invalid_reading_data() throws Exception {
        // Given - Datos inválidos
        ReadingCreateDto invalidDto = new ReadingCreateDto();
        invalidDto.setGlucoseLevel(-50.0f); // Valor negativo (inválido)
        invalidDto.setDeviceId(null); // Campo requerido nulo

        // When & Then
        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(readingService, never()).createReading(any(ReadingCreateDto.class));
    }

    // Métodos helper para crear objetos de test
    private ReadingDetailsDto createTestDto() {
        ReadingDetailsDto dto = new ReadingDetailsDto();
        dto.setId(1L);
        dto.setGlucoseLevel(100.0f);
        dto.setDeviceId(1L);
        dto.setStatus("NORMAL");
        dto.setTimestamp(LocalDateTime.now());
        dto.setManualReading(false);
        dto.setRequiresAction(false);
        return dto;
    }

    private ReadingCreateDto createTestCreateDto() {
        ReadingCreateDto dto = new ReadingCreateDto();
        dto.setGlucoseLevel(100.0f);
        dto.setDeviceId(1L);
        dto.setTimestamp(LocalDateTime.now());
        dto.setStatus(ReadingStatus.NORMAL);
        dto.setManualReading(false);
        dto.setRequiresAction(false);
        return dto;
    }

    private GlucoseStatisticsDto createTestStatistics() {
        GlucoseStatisticsDto stats = new GlucoseStatisticsDto();
        stats.setDeviceId(1L);
        stats.setDeviceSerialNo("DEV123");
        stats.setPatientName("Juan Pérez");
        stats.setStartTime(LocalDateTime.now().minusHours(1));
        stats.setEndTime(LocalDateTime.now());
        stats.setAverageGlucoseLevel(110.5f);
        stats.setLowReadingsCount(2L);
        stats.setHighReadingsCount(1L);
        stats.setLowestReading(85.0f);
        stats.setHighestReading(145.0f);
        stats.setTotalReadings(10);
        stats.setStandardDeviation(15.2f);
        return stats;
    }
}