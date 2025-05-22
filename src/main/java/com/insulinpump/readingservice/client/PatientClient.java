package com.insulinpump.readingservice.client;

import com.insulinpump.readingservice.dto.PatientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "patient-service")
public interface PatientClient {

    @GetMapping("/api/patients/{id}")
    PatientDto getPatientById(@PathVariable("id") Long id);

    @GetMapping("/api/patients/device/{deviceId}")
    PatientDto getPatientByDeviceId(@PathVariable("deviceId") Long deviceId);
}