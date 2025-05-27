
package com.insulinpump.readingservice.integration;

import com.insulinpump.readingservice.client.DeviceClient;
import com.insulinpump.readingservice.client.PatientClient;
import com.insulinpump.readingservice.dto.DeviceDto;
import com.insulinpump.readingservice.dto.PatientDto;
import com.insulinpump.readingservice.dto.ReadingCreateDto;
import com.insulinpump.readingservice.dto.ReadingDetailsDto;
import com.insulinpump.readingservice.exception.DeviceNotFoundException;
import com.insulinpump.readingservice.repository.ReadingRepository;
import com.insulinpump.readingservice.service.ReadingService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Microservice Communication Integration Tests - Standalone")
class MicroserviceCommunicationTest {

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private DeviceClient deviceClient;

    @Mock
    private PatientClient patientClient;

    @InjectMocks
    private ReadingService readingService;

    @BeforeEach
    void setUp() {
        // Setup común si es necesario
    }

    @Test
    @DisplayName("Debería crear lectura con información completa de dispositivo y paciente")
    void should_create_reading_with_complete_device_and_patient_info() {
        // Given - Configurar mocks de servicios externos
        DeviceDto mockDevice = new DeviceDto(1L, "DEV123", "Insulin Pump X", "MedTech Corp", "ACTIVE", 100L);
        PatientDto mockPatient = new PatientDto(100L, "Juan Pérez", 35, "MED123", 1L, "TYPE_1");

        when(deviceClient.getDeviceById(1L)).thenReturn(mockDevice);
        when(patientClient.getPatientById(100L)).thenReturn(mockPatient);
        when(readingRepository.save(any())).thenAnswer(invocation -> {
            var reading = invocation.getArgument(0);
            return reading; // Simular guardado
        });

        ReadingCreateDto createDto = new ReadingCreateDto();
        createDto.setGlucoseLevel(120.0f);
        createDto.setDeviceId(1L);
        createDto.setTimestamp(LocalDateTime.now());
        createDto.setManualReading(false);

        // When - Crear lectura que requiere comunicación entre servicios
        ReadingDetailsDto result = readingService.createReading(createDto);

        // Then - Verificar que se obtuvo información de todos los servicios
        assertThat(result).isNotNull();
        assertThat(result.getGlucoseLevel()).isEqualTo(120.0f);
        assertThat(result.getStatus()).isEqualTo("NORMAL");

        // Verificar información del dispositivo
        assertThat(result.getDevice()).isNotNull();
        assertThat(result.getDevice().getSerialNo()).isEqualTo("DEV123");
        assertThat(result.getDevice().getModel()).isEqualTo("Insulin Pump X");
        assertThat(result.getDevice().getManufacturer()).isEqualTo("MedTech Corp");

        // Verificar información del paciente
        assertThat(result.getPatient()).isNotNull();
        assertThat(result.getPatient().getName()).isEqualTo("Juan Pérez");
        assertThat(result.getPatient().getMedicalId()).isEqualTo("MED123");
        assertThat(result.getPatient().getDiabetesType()).isEqualTo("TYPE_1");

        // Verificar que se llamaron los servicios externos
        verify(deviceClient, times(2)).getDeviceById(1L); // Una vez para validar, otra para enriquecer
        verify(patientClient, times(1)).getPatientById(100L);
        verify(readingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Debería manejar fallo de comunicación con device-service")
    void should_handle_device_service_communication_failure() {
        // Given - Simular fallo en device-service usando excepción más simple
        when(deviceClient.getDeviceById(1L))
                .thenThrow(new RuntimeException("Device service not available"));

        ReadingCreateDto createDto = new ReadingCreateDto();
        createDto.setGlucoseLevel(95.0f);
        createDto.setDeviceId(1L);
        createDto.setTimestamp(LocalDateTime.now());

        // When & Then - Verificar que se maneja correctamente el error
        assertThatThrownBy(() -> readingService.createReading(createDto))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessageContaining("Dispositivo no encontrado con ID: 1");

        verify(deviceClient, times(1)).getDeviceById(1L);
        verify(patientClient, never()).getPatientById(any());
        verify(readingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería obtener lecturas por paciente consultando múltiples servicios")
    void should_get_readings_by_patient_consulting_multiple_services() {
        // Given - Configurar comunicación entre servicios
        PatientDto mockPatient = new PatientDto(100L, "María García", 42, "MED456", 2L, "TYPE_2");
        DeviceDto mockDevice = new DeviceDto(2L, "DEV456", "Pump Pro", "InsulinTech", "ACTIVE", 100L);

        when(patientClient.getPatientById(100L)).thenReturn(mockPatient);
        when(deviceClient.getDeviceById(2L)).thenReturn(mockDevice);
        when(readingRepository.findByDeviceId(2L)).thenReturn(java.util.Arrays.asList(
                createMockReading(1L, 140.0f, 2L)
        ));

        // When - Buscar lecturas por paciente
        var readings = readingService.getReadingsByPatientId(100L);

        // Then - Verificar comunicación correcta
        assertThat(readings).isNotEmpty();
        assertThat(readings.get(0).getDevice().getSerialNo()).isEqualTo("DEV456");

        verify(patientClient, times(1)).getPatientById(100L);
        verify(deviceClient, times(1)).getDeviceById(2L);
        verify(readingRepository, times(1)).findByDeviceId(2L);
    }

    @Test
    @DisplayName("Debería obtener estadísticas consultando información de dispositivo y paciente")
    void should_get_statistics_with_device_and_patient_info() {
        // Given - Setup completo para estadísticas
        DeviceDto mockDevice = new DeviceDto(1L, "STAT123", "Analytics Pump", "DataMed", "ACTIVE", 200L);
        PatientDto mockPatient = new PatientDto(200L, "Ana Rodríguez", 28, "MED789", 1L, "TYPE_1");

        when(deviceClient.getDeviceById(1L)).thenReturn(mockDevice);
        when(patientClient.getPatientByDeviceId(1L)).thenReturn(mockPatient);

        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now();

        // Mock de datos estadísticos
        when(readingRepository.findByDeviceIdAndTimestampBetween(eq(1L), any(), any()))
                .thenReturn(java.util.Arrays.asList(
                        createMockReading(1L, 100.0f, 1L),
                        createMockReading(2L, 120.0f, 1L)
                ));
        when(readingRepository.findAverageGlucoseLevelByDeviceIdAndTimeRange(eq(1L), any(), any()))
                .thenReturn(110.0f);
        when(readingRepository.countLowReadingsByDeviceIdAndTimeRange(eq(1L), any(), any()))
                .thenReturn(0L);
        when(readingRepository.countHighReadingsByDeviceIdAndTimeRange(eq(1L), any(), any()))
                .thenReturn(0L);
        when(readingRepository.findMinGlucoseLevelByDeviceIdAndTimeRange(eq(1L), any(), any()))
                .thenReturn(100.0f);
        when(readingRepository.findMaxGlucoseLevelByDeviceIdAndTimeRange(eq(1L), any(), any()))
                .thenReturn(120.0f);

        // When - Obtener estadísticas
        var statistics = readingService.getGlucoseStatisticsByDeviceId(1L, start, end);

        // Then - Verificar que se incluyó información de ambos servicios
        assertThat(statistics.getDeviceSerialNo()).isEqualTo("STAT123");
        assertThat(statistics.getPatientName()).isEqualTo("Ana Rodríguez");
        assertThat(statistics.getAverageGlucoseLevel()).isEqualTo(110.0f);

        verify(deviceClient, times(1)).getDeviceById(1L);
        verify(patientClient, times(1)).getPatientByDeviceId(1L);
    }

    @Test
    @DisplayName("Debería manejar paciente sin dispositivo asignado")
    void should_handle_patient_without_assigned_device() {
        // Given - Paciente sin dispositivo
        PatientDto patientWithoutDevice = new PatientDto(300L, "Carlos Sin Dispositivo", 45, "MED999", null, "TYPE_2");
        when(patientClient.getPatientById(300L)).thenReturn(patientWithoutDevice);

        // When & Then - Verificar manejo de error
        assertThatThrownBy(() -> readingService.getReadingsByPatientId(300L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El paciente no tiene un dispositivo asignado");

        verify(patientClient, times(1)).getPatientById(300L);
        verify(deviceClient, never()).getDeviceById(any());
        verify(readingRepository, never()).findByDeviceId(any());
    }

    @Test
    @DisplayName("Debería continuar funcionando si patient-service no está disponible")
    void should_continue_working_if_patient_service_unavailable() {
        // Given - Device disponible pero patient service falla
        DeviceDto mockDevice = new DeviceDto(5L, "DEV999", "Independent Pump", "SoloMed", "ACTIVE", 500L);
        when(deviceClient.getDeviceById(5L)).thenReturn(mockDevice);
        when(patientClient.getPatientById(500L)).thenThrow(new RuntimeException("Patient service unavailable"));
        when(readingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReadingCreateDto createDto = new ReadingCreateDto();
        createDto.setGlucoseLevel(115.0f);
        createDto.setDeviceId(5L);
        createDto.setTimestamp(LocalDateTime.now());

        // When - Crear lectura (debería funcionar sin info del paciente)
        ReadingDetailsDto result = readingService.createReading(createDto);

        // Then - Verificar que funciona con información parcial
        assertThat(result).isNotNull();
        assertThat(result.getDevice()).isNotNull();
        assertThat(result.getDevice().getSerialNo()).isEqualTo("DEV999");
        // El paciente puede ser null debido al error del servicio

        verify(deviceClient, times(2)).getDeviceById(5L); // Una vez para validar, otra para enriquecer
        verify(readingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Debería manejar timeout en comunicación con services externos")
    void should_handle_timeout_in_external_service_communication() {
        // Given - Simular timeout usando RuntimeException genérica o FeignException específica
        when(deviceClient.getDeviceById(1L))
                .thenThrow(new RuntimeException("Request timeout"));

        ReadingCreateDto createDto = new ReadingCreateDto();
        createDto.setGlucoseLevel(105.0f);
        createDto.setDeviceId(1L);
        createDto.setTimestamp(LocalDateTime.now());

        // When & Then - Verificar manejo de timeout
        assertThatThrownBy(() -> readingService.createReading(createDto))
                .isInstanceOf(DeviceNotFoundException.class);

        verify(deviceClient, times(1)).getDeviceById(1L);
        verify(readingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería validar dispositivo antes de crear lectura")
    void should_validate_device_before_creating_reading() {
        // Given - Device válido
        DeviceDto validDevice = new DeviceDto(10L, "VALID123", "Valid Pump", "ValidCorp", "ACTIVE", null);
        when(deviceClient.getDeviceById(10L)).thenReturn(validDevice);
        when(readingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReadingCreateDto createDto = new ReadingCreateDto();
        createDto.setGlucoseLevel(90.0f);
        createDto.setDeviceId(10L);
        createDto.setTimestamp(LocalDateTime.now());

        // When
        ReadingDetailsDto result = readingService.createReading(createDto);

        // Then - Verificar que se validó el dispositivo antes de crear
        assertThat(result).isNotNull();
        assertThat(result.getGlucoseLevel()).isEqualTo(90.0f);

        // Verificar orden de llamadas: primero validar dispositivo, luego guardar
        var inOrder = inOrder(deviceClient, readingRepository);
        inOrder.verify(deviceClient).getDeviceById(10L);
        inOrder.verify(readingRepository).save(any());
    }

    // Método helper para crear lecturas mock
    private com.insulinpump.readingservice.model.Reading createMockReading(Long id, Float glucoseLevel, Long deviceId) {
        var reading = new com.insulinpump.readingservice.model.Reading();
        reading.setId(id);
        reading.setGlucoseLevel(glucoseLevel);
        reading.setDeviceId(deviceId);
        reading.setTimestamp(LocalDateTime.now());
        reading.setStatus(com.insulinpump.readingservice.model.ReadingStatus.NORMAL);
        reading.setManualReading(false);
        reading.setRequiresAction(false);
        return reading;
    }
}