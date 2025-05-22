package com.insulinpump.readingservice.service;

import com.insulinpump.readingservice.client.DeviceClient;
import com.insulinpump.readingservice.client.PatientClient;
import com.insulinpump.readingservice.dto.*;
import com.insulinpump.readingservice.exception.DeviceNotFoundException;
import com.insulinpump.readingservice.exception.PatientNotFoundException;
import com.insulinpump.readingservice.exception.ReadingNotFoundException;
import com.insulinpump.readingservice.model.Reading;
import com.insulinpump.readingservice.model.ReadingStatus;
import com.insulinpump.readingservice.repository.ReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReadingService {

    private final ReadingRepository readingRepository;
    private final DeviceClient deviceClient;
    private final PatientClient patientClient;

    public List<ReadingDetailsDto> getAllReadings() {
        log.info("Obteniendo todas las lecturas");
        return readingRepository.findAll().stream()
                .map(this::convertToReadingDetailsDto)
                .collect(Collectors.toList());
    }

    public ReadingDetailsDto getReadingById(Long id) {
        log.info("Obteniendo lectura con ID: {}", id);
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new ReadingNotFoundException(id));
        return convertToReadingDetailsDto(reading);
    }

    public List<ReadingDetailsDto> getReadingsByDeviceId(Long deviceId) {
        log.info("Obteniendo lecturas del dispositivo con ID: {}", deviceId);
        // Verificar que el dispositivo existe
        try {
            deviceClient.getDeviceById(deviceId);
        } catch (Exception e) {
            throw new DeviceNotFoundException(deviceId);
        }

        return readingRepository.findByDeviceId(deviceId).stream()
                .map(this::convertToReadingDetailsDto)
                .collect(Collectors.toList());
    }

    public List<ReadingDetailsDto> getReadingsByPatientId(Long patientId) {
        log.info("Obteniendo lecturas del paciente con ID: {}", patientId);

        // Verificar que el paciente existe y obtener su información
        PatientDto patient;
        try {
            patient = patientClient.getPatientById(patientId);
        } catch (Exception e) {
            throw new PatientNotFoundException(patientId);
        }

        if (patient.getDeviceId() == null) {
            throw new RuntimeException("El paciente no tiene un dispositivo asignado");
        }

        return readingRepository.findByDeviceId(patient.getDeviceId()).stream()
                .map(this::convertToReadingDetailsDto)
                .collect(Collectors.toList());
    }

    public List<ReadingDetailsDto> getReadingsByDeviceIdAndTimeRange(Long deviceId, LocalDateTime start, LocalDateTime end) {
        log.info("Obteniendo lecturas del dispositivo {} entre {} y {}", deviceId, start, end);
        return readingRepository.findByDeviceIdAndTimestampBetween(deviceId, start, end).stream()
                .map(this::convertToReadingDetailsDto)
                .collect(Collectors.toList());
    }

    public List<ReadingDetailsDto> getReadingsByStatus(ReadingStatus status) {
        log.info("Obteniendo lecturas con estado: {}", status);
        return readingRepository.findByStatus(status).stream()
                .map(this::convertToReadingDetailsDto)
                .collect(Collectors.toList());
    }

    public List<ReadingDetailsDto> getReadingsRequiringAction() {
        log.info("Obteniendo lecturas que requieren acción");
        return readingRepository.findByRequiresActionTrue().stream()
                .map(this::convertToReadingDetailsDto)
                .collect(Collectors.toList());
    }

    public ReadingDetailsDto createReading(ReadingCreateDto readingCreateDto) {
        log.info("Creando nueva lectura para dispositivo: {}", readingCreateDto.getDeviceId());

        // Verificar que el dispositivo existe
        try {
            deviceClient.getDeviceById(readingCreateDto.getDeviceId());
        } catch (Exception e) {
            throw new DeviceNotFoundException(readingCreateDto.getDeviceId());
        }

        Reading reading = new Reading();
        BeanUtils.copyProperties(readingCreateDto, reading);

        // Determinar el estado si no se proporciona
        if (reading.getStatus() == null) {
            reading.setStatus(determineReadingStatus(reading.getGlucoseLevel()));
        }

        // Establecer timestamp si no se proporciona
        if (reading.getTimestamp() == null) {
            reading.setTimestamp(LocalDateTime.now());
        }

        // Determinar si requiere acción
        if (reading.getRequiresAction() == null) {
            reading.setRequiresAction(requiresAction(reading.getStatus()));
        }

        Reading savedReading = readingRepository.save(reading);
        log.info("Lectura creada exitosamente con ID: {}", savedReading.getId());

        return convertToReadingDetailsDto(savedReading);
    }

    public ReadingDetailsDto updateReading(Long id, ReadingCreateDto readingUpdateDto) {
        log.info("Actualizando lectura con ID: {}", id);

        Reading existingReading = readingRepository.findById(id)
                .orElseThrow(() -> new ReadingNotFoundException(id));

        BeanUtils.copyProperties(readingUpdateDto, existingReading, "id");

        // Recalcular estado si cambió el nivel de glucosa
        if (existingReading.getStatus() == null) {
            existingReading.setStatus(determineReadingStatus(existingReading.getGlucoseLevel()));
        }

        // Actualizar si requiere acción
        if (existingReading.getRequiresAction() == null) {
            existingReading.setRequiresAction(requiresAction(existingReading.getStatus()));
        }

        Reading updatedReading = readingRepository.save(existingReading);
        log.info("Lectura actualizada exitosamente");

        return convertToReadingDetailsDto(updatedReading);
    }

    public void deleteReading(Long id) {
        log.info("Eliminando lectura con ID: {}", id);

        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new ReadingNotFoundException(id));

        readingRepository.delete(reading);
        log.info("Lectura eliminada exitosamente");
    }

    public ReadingDetailsDto getLatestReadingByDeviceId(Long deviceId) {
        log.info("Obteniendo última lectura del dispositivo: {}", deviceId);

        Reading reading = readingRepository.findLatestByDeviceId(deviceId)
                .orElseThrow(() -> new ReadingNotFoundException("No se encontraron lecturas para el dispositivo: " + deviceId));

        return convertToReadingDetailsDto(reading);
    }

    public GlucoseStatisticsDto getGlucoseStatisticsByDeviceId(Long deviceId, LocalDateTime start, LocalDateTime end) {
        log.info("Calculando estadísticas de glucosa para dispositivo {} entre {} y {}", deviceId, start, end);

        // Verificar que el dispositivo existe
        DeviceDto device;
        try {
            device = deviceClient.getDeviceById(deviceId);
        } catch (Exception e) {
            throw new DeviceNotFoundException(deviceId);
        }

        // Obtener el paciente asociado al dispositivo
        PatientDto patient = null;
        try {
            patient = patientClient.getPatientByDeviceId(deviceId);
        } catch (Exception e) {
            log.warn("No se pudo obtener información del paciente para el dispositivo: {}", deviceId);
        }

        // Obtener estadísticas de la base de datos
        List<Reading> readings = readingRepository.findByDeviceIdAndTimestampBetween(deviceId, start, end);

        if (readings.isEmpty()) {
            throw new RuntimeException("No hay lecturas disponibles para el rango de tiempo especificado");
        }

        Float averageGlucoseLevel = readingRepository.findAverageGlucoseLevelByDeviceIdAndTimeRange(deviceId, start, end);
        Long lowReadingsCount = readingRepository.countLowReadingsByDeviceIdAndTimeRange(deviceId, start, end);
        Long highReadingsCount = readingRepository.countHighReadingsByDeviceIdAndTimeRange(deviceId, start, end);
        Float lowestReading = readingRepository.findMinGlucoseLevelByDeviceIdAndTimeRange(deviceId, start, end);
        Float highestReading = readingRepository.findMaxGlucoseLevelByDeviceIdAndTimeRange(deviceId, start, end);

        // Calcular desviación estándar
        Float standardDeviation = calculateStandardDeviation(readings, averageGlucoseLevel);

        // Crear estadísticas
        GlucoseStatisticsDto statistics = new GlucoseStatisticsDto();
        statistics.setDeviceId(deviceId);
        statistics.setDeviceSerialNo(device.getSerialNo());
        statistics.setPatientName(patient != null ? patient.getName() : "No asignado");
        statistics.setStartTime(start);
        statistics.setEndTime(end);
        statistics.setAverageGlucoseLevel(averageGlucoseLevel);
        statistics.setLowReadingsCount(lowReadingsCount);
        statistics.setHighReadingsCount(highReadingsCount);
        statistics.setLowestReading(lowestReading);
        statistics.setHighestReading(highestReading);
        statistics.setTotalReadings(readings.size());
        statistics.setStandardDeviation(standardDeviation);

        return statistics;
    }

    private ReadingDetailsDto convertToReadingDetailsDto(Reading reading) {
        ReadingDetailsDto dto = new ReadingDetailsDto(reading);

        // Obtener información del dispositivo
        try {
            DeviceDto device = deviceClient.getDeviceById(reading.getDeviceId());
            dto.setDevice(device);

            // Obtener información del paciente si el dispositivo tiene uno asignado
            if (device.getPatientId() != null) {
                try {
                    PatientDto patient = patientClient.getPatientById(device.getPatientId());
                    dto.setPatient(patient);
                } catch (Exception e) {
                    log.warn("No se pudo obtener información del paciente para la lectura: {}", reading.getId());
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener información del dispositivo para la lectura: {}", reading.getId());
        }

        return dto;
    }

    private ReadingStatus determineReadingStatus(Float glucoseLevel) {
        final float CRITICAL_LOW_THRESHOLD = 50.0f;
        final float LOW_THRESHOLD = 70.0f;
        final float HIGH_THRESHOLD = 180.0f;
        final float CRITICAL_HIGH_THRESHOLD = 250.0f;

        if (glucoseLevel < CRITICAL_LOW_THRESHOLD) {
            return ReadingStatus.CRITICAL_LOW;
        } else if (glucoseLevel < LOW_THRESHOLD) {
            return ReadingStatus.LOW;
        } else if (glucoseLevel > CRITICAL_HIGH_THRESHOLD) {
            return ReadingStatus.CRITICAL_HIGH;
        } else if (glucoseLevel > HIGH_THRESHOLD) {
            return ReadingStatus.HIGH;
        } else {
            return ReadingStatus.NORMAL;
        }
    }

    private Boolean requiresAction(ReadingStatus status) {
        return status == ReadingStatus.CRITICAL_LOW || status == ReadingStatus.CRITICAL_HIGH;
    }

    private Float calculateStandardDeviation(List<Reading> readings, Float mean) {
        if (readings.size() <= 1) return 0.0f;

        double variance = readings.stream()
                .mapToDouble(r -> Math.pow(r.getGlucoseLevel() - mean, 2))
                .average()
                .orElse(0.0);

        return (float) Math.sqrt(variance);
    }
}