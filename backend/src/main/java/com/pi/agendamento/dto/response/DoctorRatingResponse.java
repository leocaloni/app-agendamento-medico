package com.pi.agendamento.dto.response;

import java.util.Map;

// distribution: nota (1..5) -> quantidade. Sempre com as cinco chaves, para o grafico de barras.
public record DoctorRatingResponse(
        Double average,
        Long total,
        Map<Integer, Long> distribution
) {

    public static DoctorRatingResponse from(RatingStats stats, Map<Integer, Long> distribution) {
        return new DoctorRatingResponse(
                DoctorSearchProjection.round(stats.average()),
                stats.total(),
                distribution);
    }
}
