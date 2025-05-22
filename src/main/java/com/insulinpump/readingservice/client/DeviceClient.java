package com.insulinpump.readingservice.client;

import com.insulinpump.readingservice.dto.DeviceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "device-service")
public interface DeviceClient {

    @GetMapping("/api/devices/{id}")
    DeviceDto getDeviceById(@PathVariable("id") Long id);

    @GetMapping("/api/devices/patient/{patientId}")
    List<DeviceDto> getDevicesByPatientId(@PathVariable("patientId") Long patientId);
}
