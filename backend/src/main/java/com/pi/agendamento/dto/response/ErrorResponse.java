package com.pi.agendamento.dto.response;

import java.time.Instant;
import java.util.Map;

// corpo padrao de erro da api
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fields
) {

    // monta erro sem campos
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message, Map.of());
    }

    // monta erro com os campos invalidos
    public static ErrorResponse of(int status, String error, String message, Map<String, String> fields) {
        return new ErrorResponse(Instant.now(), status, error, message, fields);
    }
}