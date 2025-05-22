package com.insulinpump.readingservice.exception;

public class ReadingNotFoundException extends RuntimeException {
    public ReadingNotFoundException(String message) {
        super(message);
    }

    public ReadingNotFoundException(Long id) {
        super("Lectura no encontrada con ID: " + id);
    }
}
