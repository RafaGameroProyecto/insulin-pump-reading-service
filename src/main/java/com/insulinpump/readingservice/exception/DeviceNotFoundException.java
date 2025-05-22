package com.insulinpump.readingservice.exception;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String message) {
        super(message);
    }

    public DeviceNotFoundException(Long deviceId) {
        super("Dispositivo no encontrado con ID: " + deviceId);
    }
}
