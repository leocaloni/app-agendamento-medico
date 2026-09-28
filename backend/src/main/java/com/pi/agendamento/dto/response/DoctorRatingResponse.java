package com.pi.agendamento.dto.response;

import java.util.Map;

// nota do medico no perfil, com quantidade por nota de 1 a 5
public record DoctorRatingResponse(
        Double average,
        Long total,
        Map<Integer, Long> distribution
) {

    // monta a nota com media arredondada
    public static DoctorRatingResponse from(RatingStats stats, Map<Integer, Long> distribution) {
        return new DoctorRatingResponse(
                DoctorSearchProjection.round(stats.average()),
                stats.total(),
                distribution);
    }
}
