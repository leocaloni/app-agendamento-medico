package com.pi.agendamento.dto.response;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fields
) {

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message, Map.of());
    }

    public static ErrorResponse of(int status, String error, String message, Map<String, String> fields) {
        return new ErrorResponse(Instant.now(), status, error, message, fields);
    }
}