package com.insulinpump.readingservice.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String message) {
        super(message);
    }

    public PatientNotFoundException(Long patientId) {
        super("Paciente no encontrado con ID: " + patientId);
    }
}
