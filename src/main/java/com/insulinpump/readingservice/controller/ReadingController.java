package com.insulinpump.readingservice.controller;

import com.insulinpump.readingservice.dto.GlucoseStatisticsDto;
import com.insulinpump.readingservice.dto.ReadingCreateDto;
import com.insulinpump.readingservice.dto.ReadingDetailsDto;
import com.insulinpump.readingservice.model.ReadingStatus;
import com.insulinpump.readingservice.service.ReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
@Slf4j
public class ReadingController {

    private final ReadingService readingService;

    @GetMapping
    public ResponseEntity<List<ReadingDetailsDto>> getAllReadings() {
        log.info("GET /api/readings - Obteniendo todas las lecturas");
        List<ReadingDetailsDto> readings = readingService.getAllReadings();
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadingDetailsDto> getReadingById(@PathVariable Long id) {
        log.info("GET /api/readings/{} - Obteniendo lectura por ID", id);
        ReadingDetailsDto reading = readingService.getReadingById(id);
        return ResponseEntity.ok(reading);
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<ReadingDetailsDto>> getReadingsByDeviceId(@PathVariable Long deviceId) {
        log.info("GET /api/readings/device/{} - Obteniendo lecturas por dispositivo", deviceId);
        List<ReadingDetailsDto> readings = readingService.getReadingsByDeviceId(deviceId);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ReadingDetailsDto>> getReadingsByPatientId(@PathVariable Long patientId) {
        log.info("GET /api/readings/patient/{} - Obteniendo lecturas por paciente", patientId);
        List<ReadingDetailsDto> readings = readingService.getReadingsByPatientId(patientId);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/device/{deviceId}/latest")
    public ResponseEntity<ReadingDetailsDto> getLatestReadingByDeviceId(@PathVariable Long deviceId) {
        log.info("GET /api/readings/device/{}/latest - Obteniendo última lectura del dispositivo", deviceId);
        ReadingDetailsDto reading = readingService.getLatestReadingByDeviceId(deviceId);
        return ResponseEntity.ok(reading);
    }

    @GetMapping("/device/{deviceId}/timerange")
    public ResponseEntity<List<ReadingDetailsDto>> getReadingsByDeviceIdAndTimeRange(
            @PathVariable Long deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/readings/device/{}/timerange - Obteniendo lecturas por rango de tiempo", deviceId);
        List<ReadingDetailsDto> readings = readingService.getReadingsByDeviceIdAndTimeRange(deviceId, start, end);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/device/{deviceId}/statistics")
    public ResponseEntity<GlucoseStatisticsDto> getGlucoseStatisticsByDeviceId(
            @PathVariable Long deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/readings/device/{}/statistics - Obteniendo estadísticas de glucosa", deviceId);
        GlucoseStatisticsDto statistics = readingService.getGlucoseStatisticsByDeviceId(deviceId, start, end);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReadingDetailsDto>> getReadingsByStatus(@PathVariable ReadingStatus status) {
        log.info("GET /api/readings/status/{} - Obteniendo lecturas por estado", status);
        List<ReadingDetailsDto> readings = readingService.getReadingsByStatus(status);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/requiring-action")
    public ResponseEntity<List<ReadingDetailsDto>> getReadingsRequiringAction() {
        log.info("GET /api/readings/requiring-action - Obteniendo lecturas que requieren acción");
        List<ReadingDetailsDto> readings = readingService.getReadingsRequiringAction();
        return ResponseEntity.ok(readings);
    }

    @PostMapping
    public ResponseEntity<ReadingDetailsDto> createReading(@Valid @RequestBody ReadingCreateDto readingCreateDto) {
        log.info("POST /api/readings - Creando nueva lectura");
        ReadingDetailsDto createdReading = readingService.createReading(readingCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReading);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReadingDetailsDto> updateReading(
            @PathVariable Long id,
            @Valid @RequestBody ReadingCreateDto readingUpdateDto) {
        log.info("PUT /api/readings/{} - Actualizando lectura", id);
        ReadingDetailsDto updatedReading = readingService.updateReading(id, readingUpdateDto);
        return ResponseEntity.ok(updatedReading);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReading(@PathVariable Long id) {
        log.info("DELETE /api/readings/{} - Eliminando lectura", id);
        readingService.deleteReading(id);
        return ResponseEntity.noContent().build();
    }
}
