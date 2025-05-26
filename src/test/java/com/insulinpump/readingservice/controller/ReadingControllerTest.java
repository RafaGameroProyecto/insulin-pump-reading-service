package com.insulinpump.readingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insulinpump.readingservice.dto.ReadingCreateDto;
import com.insulinpump.readingservice.dto.ReadingDetailsDto;
import com.insulinpump.readingservice.exception.ReadingNotFoundException;
import com.insulinpump.readingservice.service.ReadingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadingController.class)
class ReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ReadingService readingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_get_all_readings() throws Exception {
        // Given
        when(readingService.getAllReadings()).thenReturn(Arrays.asList(createTestDto()));

        // When & Then
        mockMvc.perform(get("/api/readings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].glucoseLevel").value(100.0));
    }

    @Test
    void should_get_reading_by_id() throws Exception {
        // Given
        when(readingService.getReadingById(1L)).thenReturn(createTestDto());

        // When & Then
        mockMvc.perform(get("/api/readings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.glucoseLevel").value(100.0));
    }

    @Test
    void should_return_404_when_reading_not_found() throws Exception {
        // Given
        when(readingService.getReadingById(999L))
                .thenThrow(new ReadingNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/readings/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void should_create_reading() throws Exception {
        // Given
        ReadingCreateDto createDto = createTestCreateDto();
        when(readingService.createReading(any(ReadingCreateDto.class))).thenReturn(createTestDto());

        // When & Then
        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.glucoseLevel").value(100.0));
    }

    @Test
    @Transactional
    void should_delete_reading() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/readings/1"))
                .andExpect(status().isNoContent());
    }

    private ReadingDetailsDto createTestDto() {
        ReadingDetailsDto dto = new ReadingDetailsDto();
        dto.setId(1L);
        dto.setGlucoseLevel(100.0f);
        dto.setDeviceId(1L);
        dto.setStatus("NORMAL");
        dto.setTimestamp(LocalDateTime.now());
        return dto;
    }

    private ReadingCreateDto createTestCreateDto() {
        ReadingCreateDto dto = new ReadingCreateDto();
        dto.setGlucoseLevel(100.0f);
        dto.setDeviceId(1L);
        dto.setTimestamp(LocalDateTime.now());
        return dto;
    }
}