package com.pi.agendamento.dto.response;

import java.util.UUID;

// linha da query de busca: id do medico e nota agregada
public record DoctorSearchProjection(UUID doctorId, Double ratingAvg, long ratingCount) {

    // converte a linha crua da query
    public static DoctorSearchProjection from(Object[] row) {
        long ratingCount = ((Number) row[3]).longValue();
        Double ratingAvg = ratingCount > 0 ? round(((Number) row[2]).doubleValue()) : null;
        return new DoctorSearchProjection((UUID) row[0], ratingAvg, ratingCount);
    }

    static Double round(Double value) {
        return value == null ? null : Math.round(value * 10.0) / 10.0;
    }
}
